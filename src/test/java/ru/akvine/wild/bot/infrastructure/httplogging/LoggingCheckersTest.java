package ru.akvine.wild.bot.infrastructure.httplogging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLog;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLogRequest;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLogResponse;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.ActuatorLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.AllowAllMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.CompositeMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.MethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.SpringMvcMethodLoggingChecker;
import ru.akvine.wild.bot.infrastructure.httplogging.checkers.SwaggerLoggingChecker;

@DisplayName("Проверки, нужно ли логировать запрос")
class LoggingCheckersTest {

    private static MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        ServletRequestPathUtils.parseAndCache(request);
        return request;
    }

    @Test
    @DisplayName("Actuator не логируется, остальное логируется; null-запрос допустим")
    void actuatorChecker() {
        ActuatorLoggingChecker checker = new ActuatorLoggingChecker();

        assertThat(checker.shouldLogRequest(request("GET", "/actuator/health"))).isFalse();
        assertThat(checker.shouldLogResponse(request("GET", "/actuator"))).isFalse();
        assertThat(checker.shouldLogRequest(request("GET", "/orders"))).isTrue();
        assertThat(checker.shouldLogRequest(null)).isTrue();

        MockHttpServletRequest withPathInfo = request("GET", "/x");
        withPathInfo.setPathInfo("/actuator/info");
        assertThat(checker.shouldLogRequest(withPathInfo)).isFalse();
    }

    @Test
    @DisplayName("Swagger и api-docs не логируются")
    void swaggerChecker() {
        SwaggerLoggingChecker checker = new SwaggerLoggingChecker();

        assertThat(checker.shouldLogRequest(request("GET", "/swagger-ui/index.html")))
                .isFalse();
        assertThat(checker.shouldLogResponse(request("GET", "/v3/api-docs"))).isFalse();
        assertThat(checker.shouldLogRequest(request("GET", "/orders"))).isTrue();
        assertThat(checker.shouldLogRequest(null)).isTrue();

        MockHttpServletRequest withPathInfo = request("GET", "/x");
        withPathInfo.setPathInfo("/swagger-ui/index.html");
        assertThat(checker.shouldLogResponse(withPathInfo)).isFalse();
    }

    @Test
    @DisplayName("Составная проверка разрешает, только если разрешили все вложенные; вложенные составные игнорируются")
    void compositeChecker() {
        MethodLoggingChecker deny = new MethodLoggingChecker() {
            @Override
            public boolean shouldLogRequest(jakarta.servlet.http.HttpServletRequest r) {
                return false;
            }

            @Override
            public boolean shouldLogResponse(jakarta.servlet.http.HttpServletRequest r) {
                return false;
            }
        };
        MockHttpServletRequest request = request("GET", "/orders");

        CompositeMethodLoggingChecker allowed =
                new CompositeMethodLoggingChecker(List.of(new AllowAllMethodLoggingChecker()));
        CompositeMethodLoggingChecker denied =
                new CompositeMethodLoggingChecker(List.of(new AllowAllMethodLoggingChecker(), deny));
        CompositeMethodLoggingChecker nested = new CompositeMethodLoggingChecker(List.of(denied));

        assertThat(allowed.shouldLogRequest(request)).isTrue();
        assertThat(allowed.shouldLogResponse(request)).isTrue();
        assertThat(denied.shouldLogRequest(request)).isFalse();
        assertThat(denied.shouldLogResponse(request)).isFalse();
        assertThat(nested.shouldLogRequest(request)).isTrue();
    }

    @Test
    @DisplayName("SpringMvc-проверка не логирует методы с @DoNotLogRequest / @DoNotLogResponse / @DoNotLog")
    void springMvcChecker() throws Exception {
        RequestMappingHandlerMapping mapping = mock(RequestMappingHandlerMapping.class);
        when(mapping.getHandlerMethods())
                .thenReturn(Map.of(
                        info("/no-request"), handler("noRequest"),
                        info("/no-response"), handler("noResponse"),
                        info("/no-log"), handler("noLog"),
                        info("/normal"), handler("normal"),
                        info("/class-level"),
                                new HandlerMethod(
                                        new AnnotatedController(), method(AnnotatedController.class, "any"))));
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestMappingHandlerMapping> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(invocation -> Stream.of(mapping));
        SpringMvcMethodLoggingChecker checker = new SpringMvcMethodLoggingChecker(provider);

        assertThat(checker.shouldLogRequest(request("GET", "/no-request"))).isFalse();
        assertThat(checker.shouldLogResponse(request("GET", "/no-request"))).isTrue();
        assertThat(checker.shouldLogRequest(request("GET", "/no-response"))).isTrue();
        assertThat(checker.shouldLogResponse(request("GET", "/no-response"))).isFalse();
        assertThat(checker.shouldLogRequest(request("GET", "/no-log"))).isFalse();
        assertThat(checker.shouldLogResponse(request("GET", "/no-log"))).isFalse();
        assertThat(checker.shouldLogRequest(request("GET", "/normal"))).isTrue();
        assertThat(checker.shouldLogResponse(request("GET", "/normal"))).isTrue();
        assertThat(checker.shouldLogRequest(request("GET", "/class-level"))).isFalse();
        assertThat(checker.shouldLogRequest(request("GET", "/unknown"))).isTrue();
    }

    @Test
    @DisplayName("Фабрика собирает фильтр с чёрным списком заголовков и проверками по настройкам")
    void factoryBuildsFilter() {
        HttpLoggingProperties properties = new HttpLoggingProperties();
        properties.setIgnoreActuator(true);
        properties.setIgnoreSwagger(true);
        properties.setMaxPayloadLength(10);
        properties.setHeadersBlackList(java.util.Set.of("x-custom"));
        properties.setRequestBodyExcludePaths(List.of("/upload"));
        properties.setResponseBodyExcludeContentTypes(List.of("application/pdf"));
        @SuppressWarnings("unchecked")
        ObjectProvider<RequestMappingHandlerMapping> provider = mock(ObjectProvider.class);

        HttpLoggingFilter filter = HttpLoggingFilterFactory.create(properties, provider);

        assertThat(filter).isNotNull();
        assertThat(properties.getLevel().name()).isEqualTo("INFO");
        assertThat(properties.isEnabled()).isFalse();
    }

    private static RequestMappingInfo info(String path) {
        return RequestMappingInfo.paths(path).build();
    }

    private static HandlerMethod handler(String name) throws Exception {
        return new HandlerMethod(new Controller(), method(Controller.class, name));
    }

    private static Method method(Class<?> type, String name) throws Exception {
        return type.getDeclaredMethod(name);
    }

    static class Controller {
        @DoNotLogRequest
        public void noRequest() {}

        @DoNotLogResponse
        public void noResponse() {}

        @DoNotLog
        public void noLog() {}

        public void normal() {}
    }

    @DoNotLogRequest
    static class AnnotatedController {
        public void any() {}
    }
}
