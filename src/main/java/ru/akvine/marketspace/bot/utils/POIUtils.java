package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Workbook;
import ru.akvine.marketspace.bot.exceptions.POIException;

import java.io.ByteArrayOutputStream;

@UtilityClass
public class POIUtils {
    public byte[] mapToBytes(Workbook workbook) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception ex) {
            throw new POIException(ex);
        }
    }
}
