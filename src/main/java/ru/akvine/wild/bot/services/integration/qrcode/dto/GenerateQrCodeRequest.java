package ru.akvine.wild.bot.services.integration.qrcode.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenerateQrCodeRequest {
    private String url;
    private String errorCorrectionLevel;
    private Double cornerBlockRadiusFactor;
    private Integer radiusFactor;
    private Integer qrSize;
    private int borderSize;
    private String imageType;
    private boolean roundInnerCorners;
    private boolean roundOuterCorners;
    private boolean cornerBlocksAsCircles;
}
