package ru.akvine.wild.bot.infrastructure.httplogging;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Запоминает первые {@code maxPayloadLength} байт тела ответа, не меняя то, что уходит клиенту
 */
class CachingResponseWrapper extends HttpServletResponseWrapper {
    private final int maxPayloadLength;
    private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private Writer cacheWriter;

    CachingResponseWrapper(HttpServletResponse response, int maxPayloadLength) {
        super(response);
        this.maxPayloadLength = maxPayloadLength;
    }

    byte[] cachedBody() {
        return cachedBody.toByteArray();
    }

    /**
     * Дописывает в кэш символы, ещё лежащие в буфере кодировщика {@link #getWriter()}
     */
    void flushCache() {
        if (cacheWriter != null) {
            try {
                cacheWriter.flush();
            } catch (IOException ignored) {
                // кэш только для лога
            }
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        if (outputStream == null) {
            outputStream = new CachingServletOutputStream(super.getOutputStream());
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        if (writer == null) {
            PrintWriter delegate = super.getWriter();
            String encoding = getCharacterEncoding();
            Charset charset = encoding != null ? Charset.forName(encoding) : StandardCharsets.ISO_8859_1;
            cacheWriter = new OutputStreamWriter(new LimitedCacheStream(), charset);
            writer = new PrintWriter(new TeeWriter(delegate, cacheWriter));
        }
        return writer;
    }

    @Override
    public void reset() {
        super.reset();
        cachedBody.reset();
    }

    @Override
    public void resetBuffer() {
        super.resetBuffer();
        cachedBody.reset();
    }

    private class LimitedCacheStream extends java.io.OutputStream {
        @Override
        public void write(int b) {
            if (cachedBody.size() < maxPayloadLength) {
                cachedBody.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) {
            int remaining = maxPayloadLength - cachedBody.size();
            if (remaining > 0) {
                cachedBody.write(b, off, Math.min(len, remaining));
            }
        }
    }

    private class CachingServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream delegate;
        private final LimitedCacheStream cache = new LimitedCacheStream();

        private CachingServletOutputStream(ServletOutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            cache.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            cache.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener);
        }
    }

    private static class TeeWriter extends Writer {
        private final Writer delegate;
        private final Writer copy;

        private TeeWriter(Writer delegate, Writer copy) {
            this.delegate = delegate;
            this.copy = copy;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            delegate.write(cbuf, off, len);
            copy.write(cbuf, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
            copy.flush();
        }

        @Override
        public void close() throws IOException {
            copy.flush();
            delegate.close();
        }
    }
}
