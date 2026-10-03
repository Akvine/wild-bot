package ru.akvine.wild.bot.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.exceptions.PropertiesLoadException;
import ru.akvine.wild.bot.services.property.PropertyServiceImpl;

@DisplayName("PropertyServiceImpl: настройки приложения из файла профиля")
class PropertyServiceImplTest {
    private static final String CONTENT = String.join(
            "\n",
            "text.value=hello",
            "int.value=42",
            "long.value=9000000000",
            "bool.value=true",
            "double.value=2.5",
            "list.value=1,2,3",
            "words.value=a;b;a");

    private static PropertyServiceImpl service(String content, String... profiles) {
        ResourceLoader loader = new ResourceLoader() {
            @Override
            public Resource getResource(String location) {
                return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
                    @Override
                    public String getDescription() {
                        return location;
                    }
                };
            }

            @Override
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();
            }
        };
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        PropertyServiceImpl service = new PropertyServiceImpl(environment, loader);
        ReflectionTestUtils.invokeMethod(service, "init");
        return service;
    }

    @Test
    @DisplayName("Значения читаются как строка и приводятся к Integer, Long, Boolean, Double")
    void typedAccess() {
        PropertyServiceImpl service = service(CONTENT);

        assertThat(service.get("text.value")).isEqualTo("hello");
        assertThat(service.getAs("int.value", Integer.class)).isEqualTo(42);
        assertThat(service.getAs("long.value", Long.class)).isEqualTo(9_000_000_000L);
        assertThat(service.getAs("bool.value", Boolean.class)).isTrue();
        assertThat(service.getAs("double.value", Double.class)).isEqualTo(2.5);
        assertThat(service.getAs("text.value", String.class)).isEqualTo("hello");
        assertThat(service.contains("int.value")).isTrue();
        assertThat(service.contains("missing")).isFalse();
        assertThat(service.getAll()).containsEntry("text.value", "hello");
        assertThatThrownBy(() -> service.getAll().put("x", "y")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Отсутствующий ключ и неподдерживаемый тип - понятные ошибки")
    void errors() {
        PropertyServiceImpl service = service(CONTENT);

        assertThatThrownBy(() -> service.get("missing")).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.get(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> service.getAs("missing", String.class)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.getAs("int.value", java.math.BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getAsList("list.value", ",", java.math.BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Списки и множества: строки и типизированные значения")
    void collections() {
        PropertyServiceImpl service = service(CONTENT);

        assertThat(service.getAsList("list.value", ",")).containsExactly("1", "2", "3");
        assertThat(service.getAsList("list.value", ",", Integer.class)).containsExactly(1, 2, 3);
        assertThat(service.getAsList("list.value", ",", String.class)).containsExactly("1", "2", "3");
        assertThat(service.getAsSet("words.value", ";")).containsExactlyInAnyOrder("a", "b");
        assertThat(service.getAsSet("list.value", ",", Integer.class)).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    @DisplayName("put добавляет и заменяет значения; null-значение даёт пустые коллекции")
    void putAndNullValues() {
        PropertyServiceImpl service = service(CONTENT);

        service.put("new.value", "1");
        service.put("int.value", "7");
        service.put("nullable.value", null);

        assertThat(service.get("new.value")).isEqualTo("1");
        assertThat(service.getAs("int.value", Integer.class)).isEqualTo(7);
        assertThat(service.getAsList("nullable.value", ",")).isEmpty();
        assertThat(service.getAsList("nullable.value", ",", Integer.class)).isEmpty();
        assertThat(service.getAsSet("nullable.value", ",")).isEmpty();
        assertThat(service.getAs("nullable.value", Integer.class)).isZero();
        assertThat(service.getAs("nullable.value", Double.class)).isZero();
    }

    @Test
    @DisplayName("Профиль выбирает файл настроек; два профиля сразу и нечитаемый файл - ошибки")
    void profiles() {
        assertThat(service(CONTENT, "local").get("text.value")).isEqualTo("hello");

        assertThatThrownBy(() -> service(CONTENT, "local", "prod")).isInstanceOf(PropertiesLoadException.class);

        ResourceLoader broken = new org.springframework.core.io.DefaultResourceLoader() {
            @Override
            public Resource getResource(String location) {
                return new ByteArrayResource(new byte[0]) {
                    @Override
                    public java.io.InputStream getInputStream() throws java.io.IOException {
                        throw new java.io.IOException("no file");
                    }
                };
            }
        };
        PropertyServiceImpl service = new PropertyServiceImpl(new MockEnvironment(), broken);
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "init"))
                .isInstanceOf(PropertiesLoadException.class)
                .hasMessageContaining("no file");
    }
}
