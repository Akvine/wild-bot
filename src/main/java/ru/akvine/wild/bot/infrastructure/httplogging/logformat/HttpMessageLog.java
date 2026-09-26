package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

import java.io.UnsupportedEncodingException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
abstract class HttpMessageLog {
    private static final String UNKNOWN_BODY_ENCODING = "[unknown encoding]";
    private byte[] body;
    private Map<String, List<String>> httpHeaders;
    private String encoding;
    private boolean unknownToHex = false;

    String bodyAsString() {
        if (body == null) {
            return "";
        }

        String bodyAsString;
        try {
            if (encoding != null && !encoding.isEmpty()) {
                bodyAsString = new String(body, encoding);
            } else if (unknownToHex) {
                bodyAsString = HexFormat.of().formatHex(body);
            } else {
                return UNKNOWN_BODY_ENCODING;
            }
        } catch (UnsupportedEncodingException ex) {
            bodyAsString = unknownToHex ? HexFormat.of().formatHex(body) : UNKNOWN_BODY_ENCODING;
        }

        return bodyAsString;
    }
}
