package ru.akvine.wild.bot.unit.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import ru.akvine.wild.bot.infrastructure.role.AppRole;
import ru.akvine.wild.bot.infrastructure.role.ConditionalOnAppRole;
import ru.akvine.wild.bot.infrastructure.role.ConditionalOnJobs;

class AppRoleTest {

    @Test
    @DisplayName("Значение разбирается без учёта регистра и пробелов")
    void parsesCaseInsensitive() {
        assertThat(AppRole.of(" Worker ")).isEqualTo(AppRole.WORKER);
        assertThat(AppRole.of("web")).isEqualTo(AppRole.WEB);
        assertThat(AppRole.of("ALL")).isEqualTo(AppRole.ALL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Пустое значение - роль all")
    void emptyMeansAll(String value) {
        assertThat(AppRole.of(value)).isEqualTo(AppRole.ALL);
    }

    @Test
    @DisplayName("Опечатка в роли - ошибка, а не тихий запуск в другой роли")
    void typoFailsFast() {
        assertThatThrownBy(() -> AppRole.of("workr")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Какая роль что делает")
    void capabilities() {
        assertThat(AppRole.ALL.runsJobs()).isTrue();
        assertThat(AppRole.ALL.servesTraffic()).isTrue();
        assertThat(AppRole.WEB.runsJobs()).isFalse();
        assertThat(AppRole.WEB.servesTraffic()).isTrue();
        assertThat(AppRole.WORKER.runsJobs()).isTrue();
        assertThat(AppRole.WORKER.servesTraffic()).isFalse();
    }

    @Test
    @DisplayName("@ConditionalOnJobs: бин есть в all и worker, нет в web")
    void jobsBeanPerRole() {
        assertThat(hasBean(null, "jobBean")).isTrue();
        assertThat(hasBean("all", "jobBean")).isTrue();
        assertThat(hasBean("worker", "jobBean")).isTrue();
        assertThat(hasBean("web", "jobBean")).isFalse();
    }

    @Test
    @DisplayName("@ConditionalOnAppRole(WEB): бин только в web")
    void webOnlyBean() {
        assertThat(hasBean("web", "webBean")).isTrue();
        assertThat(hasBean("worker", "webBean")).isFalse();
        assertThat(hasBean("all", "webBean")).isFalse();
    }

    private boolean hasBean(String role, String beanName) {
        MockEnvironment environment = new MockEnvironment();
        if (role != null) {
            environment.setProperty(AppRole.PROPERTY, role);
        }
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(environment);
            context.register(TestConfig.class);
            context.refresh();
            return context.containsBean(beanName);
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        @ConditionalOnJobs
        String jobBean() {
            return "job";
        }

        @Bean
        @ConditionalOnAppRole(AppRole.WEB)
        String webBean() {
            return "web";
        }
    }
}
