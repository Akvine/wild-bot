package ru.akvine.wild.bot.infrastructure.http.log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * Ответ, целиком вычитанный в память, чтобы его тело можно было и залогировать, и отдать дальше
 */
public final class LoggingHttpResponseWrapper implements ClientHttpResponse {

    private final HttpStatusCode statusCode;
    private final String statusText;
    private final HttpHeaders headers;
    private final byte[] body;

    public LoggingHttpResponseWrapper(ClientHttpResponse response) {
        try {
            statusCode = response.getStatusCode();
            statusText = response.getStatusText();
            headers = response.getHeaders();
            body = StreamUtils.copyToByteArray(response.getBody());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            response.close();
        }
    }

    @Override
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public String getStatusText() {
        return statusText;
    }

    @Override
    public void close() {}

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(this.body);
    }

    public byte[] getBodyBytes() {
        return body;
    }
}
