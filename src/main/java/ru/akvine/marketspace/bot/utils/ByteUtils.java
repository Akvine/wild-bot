package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.akvine.marketspace.bot.exceptions.ByteConvertException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@UtilityClass
public class ByteUtils {
    private static final int BUFFER_SIZE = 1024;

    public byte[] convertToBytes(@NotNull InputStream inputStream) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new ByteConvertException("Error while converting inputStream to bytes array, ex = " + exception.getMessage());
        }
    }
}
