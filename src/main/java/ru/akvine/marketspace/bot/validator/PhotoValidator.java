package ru.akvine.marketspace.bot.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.exceptions.ImageReadException;
import ru.akvine.marketspace.bot.exceptions.PhotoDimensionsValidationException;
import ru.akvine.marketspace.bot.exceptions.PhotoSizeValidationException;
import ru.akvine.marketspace.bot.utils.InformationAmountUtils;
import ru.akvine.marketspace.bot.utils.MathUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Component
public class PhotoValidator {
    @Value("${photo.width.min.pixels}")
    private int minWidth;
    @Value("${photo.height.min.pixels}")
    private int minHeight;
    @Value("${photo.max.size.megabytes}")
    private int maxMegabytesSize;

    public void validate(byte[] photo) {
        if (photo.length > InformationAmountUtils.fromMegabytesToBytes(maxMegabytesSize)) {
            double currentMegabytes = MathUtils.round(InformationAmountUtils.fromBytesToMegabytes(photo.length), 2);
            String errorMessage = String.format(
                    "Invalid image size, current megabytes = %s, max = %s",
                    currentMegabytes, maxMegabytesSize
            );
            throw new PhotoSizeValidationException(errorMessage, currentMegabytes);
        }

        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(photo));
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while read by ImageIO downloaded photo, message = %s",
                    exception.getMessage()
            );
            throw new ImageReadException(errorMessage);
        }

        if (image.getHeight() < minHeight || image.getWidth() < minWidth) {
            throw new PhotoDimensionsValidationException(
                    String.format("Photo width or height is too small! Current=%sx%s", image.getWidth(), image.getHeight()),
                    image.getWidth(),
                    image.getHeight()
            );
        }
    }
}
