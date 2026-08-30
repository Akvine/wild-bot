package ru.akvine.wild.bot.services.integration.max.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UploadFileAtServerResponse {
    private String token;
    private long fileId;
}
