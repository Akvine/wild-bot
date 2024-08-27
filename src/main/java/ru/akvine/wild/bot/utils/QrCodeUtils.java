package ru.akvine.wild.bot.utils;

import io.nayuki.qrcodegen.QrCode;
import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.exceptions.ByteConvertException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@UtilityClass
public class QrCodeUtils {
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
                int rgbColor = color ? 0x000000 : 0xFFFFFF; // Устанавливаем цвет: черный (0x000000) или белый (0xFFFFFF)
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        image.setRGB(x * scale + dx, y * scale + dy, rgbColor); // Заполняем квадрат пикселей
                    }
                }
            }
        }

        return image;
    }

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
