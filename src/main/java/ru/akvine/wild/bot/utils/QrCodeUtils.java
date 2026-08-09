package ru.akvine.wild.bot.utils;

import io.nayuki.qrcodegen.QrCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.exceptions.ByteConvertException;

/**
 * Рендер QR-кода ({@link QrCode}) в растровое изображение и PNG-байты для отправки клиенту.
 */
@UtilityClass
public class QrCodeUtils {
    /**
     * Рендерит QR-код в чёрно-белое растровое изображение с рамкой по краям.
     *
     * @param qr QR-код, не {@code null}
     * @return изображение QR-кода
     * @throws IllegalArgumentException если {@code qr} равен {@code null}
     */
    public BufferedImage convertQrCodeToImage(QrCode qr) {
        if (qr == null) {
            throw new IllegalArgumentException("Qr code can't be null");
        }

        int border = 4;
        int scale = 10;
        int size = qr.size + border * 2; // Общий размер изображения с учетом рамки
        BufferedImage image = new BufferedImage(size * scale, size * scale, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean color = (x >= border && x < size - border && y >= border && y < size - border)
                        && qr.getModule(x - border, y - border); // Получаем цвет модуля (белый или черный)
                int rgbColor =
                        color ? 0x000000 : 0xFFFFFF; // Устанавливаем цвет: черный (0x000000) или белый (0xFFFFFF)
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        image.setRGB(x * scale + dx, y * scale + dy, rgbColor); // Заполняем квадрат пикселей
                    }
                }
            }
        }

        return image;
    }

    /**
     * Рендерит QR-код и кодирует его в PNG.
     *
     * @param qrCode QR-код, не {@code null}
     * @return PNG-изображение QR-кода в виде массива байт
     * @throws IllegalArgumentException если {@code qrCode} равен {@code null}
     * @throws ByteConvertException     если при кодировании в PNG произошла ошибка
     */
    public byte[] convertQrCodeToBytes(QrCode qrCode) {
        BufferedImage image = convertQrCodeToImage(qrCode);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (Exception exception) {
            throw new ByteConvertException("Error while convert qr code to image. Error = " + exception.getMessage());
        }
        return baos.toByteArray();
    }
}
