package ru.akvine.wild.bot.services.integration.max.dto.response;

import org.springframework.core.io.ByteArrayResource;

public class MultipartByteArrayResource extends ByteArrayResource {
    private String filename;

    public MultipartByteArrayResource(byte[] byteArray, String filename) {
        super(byteArray);
        this.filename = filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }
}
