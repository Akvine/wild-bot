package ru.akvine.wild.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.utils.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Byte utils tests")
public class ByteUtilsTest {
    @Test
    @DisplayName("InputStream can't be null")
    public void input_stream_cant_be_null() {
        assertThatThrownBy(() -> ByteUtils.convertToBytes(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Convert input stream to bytes")
    public void convert_input_stream_to_bytes() {
        byte[] expected = {1, 2, 3};
        InputStream inputStream = new ByteArrayInputStream(expected);

        assertThat(ByteUtils.convertToBytes(inputStream)).isEqualTo(expected);
    }
}
