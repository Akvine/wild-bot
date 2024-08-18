package ru.akvine.marketspace.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UUID Generator tests")
public class UUIDGeneratorTest {

    @Test
    @DisplayName("Generate uuid")
    public void test_generate_uuid() {
        String uuid = UUIDGenerator.uuid();

        assertThat(uuid).isNotNull();
        assertThat(uuid).isUpperCase();
    }

    @Test
    @DisplayName("Generate uuid with specified length")
    public void test_generate_uuid_with_specified_length() {
        int length = 8;
        String uuid = UUIDGenerator.uuid(length);

        assertThat(uuid).isNotNull();
        assertThat(uuid.length()).isEqualTo(length);
    }

    @Test
    @DisplayName("Generate uuid without dashes")
    public void test_generate_uuid_without_dashes() {
        String uuid = UUIDGenerator.uuidWithoutDashes();

        assertThat(uuid).isNotNull();
        assertThat(uuid).doesNotContain("-");
    }
}
