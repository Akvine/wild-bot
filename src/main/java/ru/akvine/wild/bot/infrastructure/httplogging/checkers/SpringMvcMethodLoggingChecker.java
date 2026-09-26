package ru.akvine.wild.bot.infrastructure.httplogging.checkers;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLogRequest;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLogResponse;

/**
 * Не логирует запросы/ответы методов контроллеров, помеченных {@link DoNotLogRequest},
 * {@link DoNotLogResponse} или {@link ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLog}.
 * Список помеченных методов собирается лениво при первом запросе, когда все маппинги уже зарегистрированы
 */
@Slf4j
public class SpringMvcMethodLoggingChecker implements MethodLoggingChecker {
    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappings;
    private volatile Map<Class<? extends Annotation>, List<RequestMappingInfo>> annotated;

    public SpringMvcMethodLoggingChecker(ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    @Override
    public boolean shouldLogRequest(HttpServletRequest request) {
        return checkMethodIsNotAnnotated(request, DoNotLogRequest.class);
    }

    @Override
    public boolean shouldLogResponse(HttpServletRequest request) {
        return checkMethodIsNotAnnotated(request, DoNotLogResponse.class);
    }

    private boolean checkMethodIsNotAnnotated(HttpServletRequest request, Class<? extends Annotation> annotationType) {
        return annotated().get(annotationType).stream().noneMatch(info -> matches(info, request));
    }

    private boolean matches(RequestMappingInfo info, HttpServletRequest request) {
        try {
            return info.getMatchingCondition(request) != null;
        } catch (Exception e) {
            logger.debug("can't match request with mapping [{}]", info, e);
            return false;
        }
    }

    private Map<Class<? extends Annotation>, List<RequestMappingInfo>> annotated() {
        Map<Class<? extends Annotation>, List<RequestMappingInfo>> local = annotated;
        if (local == null) {
            local = Map.of(
                    DoNotLogRequest.class, collect(DoNotLogRequest.class),
                    DoNotLogResponse.class, collect(DoNotLogResponse.class));
            annotated = local;
        }
        return local;
    }

    private List<RequestMappingInfo> collect(Class<? extends Annotation> annotationType) {
        List<RequestMappingInfo> result = new ArrayList<>();
        handlerMappings.orderedStream().forEach(mapping -> mapping.getHandlerMethods()
                .forEach((info, handler) -> {
                    if (isAnnotated(handler, annotationType)) {
                        result.add(info);
                    }
                }));
        return result;
    }

    private boolean isAnnotated(HandlerMethod handler, Class<? extends Annotation> annotationType) {
        return AnnotationUtils.findAnnotation(handler.getMethod(), annotationType) != null
                || AnnotationUtils.findAnnotation(handler.getBeanType(), annotationType) != null;
    }
}
