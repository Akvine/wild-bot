package ru.akvine.wild.bot.utils;

import java.io.ByteArrayOutputStream;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Workbook;
import ru.akvine.wild.bot.exceptions.POIException;

/**
 * Конвертация Excel-книги (Apache POI {@link Workbook}) в массив байт для отправки клиенту.
 */
@UtilityClass
public class POIUtils {
    /**
     * Сериализует книгу Excel в массив байт.
     *
     * @param workbook книга, не {@code null}
     * @return содержимое книги в виде массива байт
     * @throws IllegalArgumentException если {@code workbook} равен {@code null}
     * @throws POIException             если при записи книги произошла ошибка
     */
    public byte[] mapToBytes(Workbook workbook) {
        validate(workbook);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exception) {
            throw new POIException(exception);
        }
    }

    /**
     * Проверяет, что переданная книга не {@code null}.
     *
     * @param workbook проверяемая книга
     * @throws IllegalArgumentException если {@code workbook} равен {@code null}
     */
    private void validate(Workbook workbook) {
        if (workbook == null) {
            throw new IllegalArgumentException("Error while map workbook to bytes. Workbook can't be null!");
        }
    }
}
