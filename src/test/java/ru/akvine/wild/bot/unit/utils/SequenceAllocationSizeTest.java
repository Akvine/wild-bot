package ru.akvine.wild.bot.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Hibernate считает, что последовательность шагает на {@code allocationSize}. Если шаг в БД другой, id повторяются
 * или уходят в минус, а {@code ddl-auto=validate} может не пропустить схему. Тест сверяет аннотации сущностей с
 * боевым changelog и с тестовой схемой.
 */
class SequenceAllocationSizeTest {
    private static final Pattern CREATE_SEQUENCE = Pattern.compile(
            "CREATE\\s+SEQUENCE\\s+(\\w+)\\s+START\\s+WITH\\s+\\d+\\s+INCREMENT\\s+BY\\s+(\\d+)",
            Pattern.CASE_INSENSITIVE);

    @Test
    @DisplayName("allocationSize каждой сущности равен INCREMENT BY последовательности в changelog")
    void matchesChangelog() throws IOException {
        assertMatches(sequencesFrom("/db/changelog/database-changelog.sql"));
    }

    @Test
    @DisplayName("allocationSize каждой сущности равен INCREMENT BY последовательности в тестовой схеме")
    void matchesTestSchema() throws IOException {
        assertMatches(sequencesFrom("/db/db-test-init.sql"));
    }

    private void assertMatches(Map<String, Integer> sequences) {
        Map<String, Integer> allocations = allocationSizes();
        assertThat(allocations).isNotEmpty();

        List<String> problems = new ArrayList<>();
        allocations.forEach((sequence, allocationSize) -> {
            Integer increment = sequences.get(sequence);
            if (increment == null) {
                problems.add(sequence + ": последовательности нет в схеме");
            } else if (!increment.equals(allocationSize)) {
                problems.add(sequence + ": allocationSize=" + allocationSize + ", INCREMENT BY " + increment);
            }
        });
        assertThat(problems).isEmpty();
    }

    private Map<String, Integer> allocationSizes() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        Map<String, Integer> result = new TreeMap<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("ru.akvine.wild.bot.entities")) {
            Class<?> entity = classOf(definition.getBeanClassName());
            for (Field field : entity.getDeclaredFields()) {
                SequenceGenerator generator = field.getAnnotation(SequenceGenerator.class);
                if (generator != null) {
                    result.put(generator.sequenceName().toUpperCase(Locale.ROOT), generator.allocationSize());
                }
            }
            SequenceGenerator classLevel = entity.getAnnotation(SequenceGenerator.class);
            if (classLevel != null) {
                result.put(classLevel.sequenceName().toUpperCase(Locale.ROOT), classLevel.allocationSize());
            }
        }
        return result;
    }

    private Map<String, Integer> sequencesFrom(String resource) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            assertThat(stream).as("resource " + resource).isNotNull();
            Matcher matcher = CREATE_SEQUENCE.matcher(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            Map<String, Integer> result = new TreeMap<>();
            while (matcher.find()) {
                result.put(matcher.group(1).toUpperCase(Locale.ROOT), Integer.parseInt(matcher.group(2)));
            }
            return result;
        }
    }

    private static Class<?> classOf(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
