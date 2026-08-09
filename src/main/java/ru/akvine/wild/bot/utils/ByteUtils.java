package ru.akvine.wild.bot.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import ru.akvine.wild.bot.exceptions.ByteConvertException;

/**
 * Конвертация {@link InputStream} в массив байт.
 */
@UtilityClass
public class ByteUtils {
    private static final int BUFFER_SIZE = 1024;

    /**
     * Читает переданный поток целиком и возвращает его содержимое как массив байт.
     *
     * @param inputStream исходный поток, не {@code null}
     * @return содержимое потока
     * @throws IllegalArgumentException если {@code inputStream} равен {@code null}
     * @throws ByteConvertException     если при чтении потока произошла ошибка
     */
    public byte[] convertToBytes(@NotNull InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream can't be null");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new ByteConvertException(
                    "Error while converting inputStream to bytes array, ex = " + exception.getMessage());
        }
    }
}
