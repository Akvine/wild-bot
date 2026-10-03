package ru.akvine.wild.bot.infrastructure.role;

import java.util.Arrays;
import java.util.Map;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Условие для {@link ConditionalOnAppRole}: роль процесса берётся из {@code server.app.role} (по умолчанию {@code all}).
 * Работает и через мета-аннотации (например, {@link ConditionalOnJobs}).
 */
class OnAppRoleCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnAppRole.class.getName());
        if (attributes == null) {
            return true;
        }
        AppRole current = AppRole.of(context.getEnvironment().getProperty(AppRole.PROPERTY, AppRole.DEFAULT_VALUE));
        return Arrays.stream((Object[]) attributes.get("value"))
                .anyMatch(allowed -> current.name().equals(allowed.toString()));
    }
}
