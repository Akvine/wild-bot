package ru.akvine.wild.bot.infrastructure.httplogging;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Запоминает первые {@code maxPayloadLength} байт тела запроса по мере того, как их вычитывает приложение
 */
class CachingRequestWrapper extends HttpServletRequestWrapper {
    private final int maxPayloadLength;
    private final HttpRequestLoggingContext context;
    private ServletInputStream inputStream;
    private BufferedReader reader;

    CachingRequestWrapper(HttpServletRequest request, int maxPayloadLength, HttpRequestLoggingContext context) {
        super(request);
        this.maxPayloadLength = maxPayloadLength;
        this.context = context;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (reader != null) {
            throw new IllegalStateException("getReader() has already been called for this request");
        }
        if (inputStream == null) {
            inputStream = new CachingServletInputStream(super.getInputStream());
        }
        return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (inputStream != null) {
            throw new IllegalStateException("getInputStream() has already been called for this request");
        }
        if (reader == null) {
            String encoding = getCharacterEncoding();
            Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.ISO_8859_1;
            reader = new BufferedReader(new InputStreamReader(new CachingServletInputStream(super.getInputStream()), charset));
        }
        return reader;
    }

    private class CachingServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;

        private CachingServletInputStream(ServletInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            int b = delegate.read();
            if (b == -1) {
                context.readFinished();
            } else {
                cache(new byte[] {(byte) b}, 0, 1);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = delegate.read(b, off, len);
            if (read == -1) {
                context.readFinished();
            } else if (read > 0) {
                cache(b, off, read);
            }
            return read;
        }

        private void cache(byte[] source, int offset, int len) {
            int remaining = maxPayloadLength - context.getCachedBodySize();
            if (remaining > 0) {
                context.writeToBodyCache(source, offset, Math.min(len, remaining));
            }
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }
    }
}
