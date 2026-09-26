package ru.akvine.wild.bot.infrastructure.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Отпечаток запроса: SHA-256 от частей запроса (метод, путь, тело). Два запроса с одинаковыми частями
 * дают один отпечаток, любое отличие - другой.
 */
public final class Fingerprints {

    private Fingerprints() {}

    /**
     * @param parts части запроса; {@code null} считается пустой строкой. Границы частей учитываются, так что
     *        {@code ("ab", "c")} и {@code ("a", "bc")} дают разные отпечатки
     */
    public static String of(String... parts) {
        MessageDigest digest = digest();
        for (String part : parts) {
            update(digest, part == null ? new byte[0] : part.getBytes(StandardCharsets.UTF_8));
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Отпечаток текстовых частей и тела запроса
     */
    public static String of(byte[] body, String... parts) {
        MessageDigest digest = digest();
        for (String part : parts) {
            update(digest, part == null ? new byte[0] : part.getBytes(StandardCharsets.UTF_8));
        }
        update(digest, body == null ? new byte[0] : body);
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void update(MessageDigest digest, byte[] bytes) {
        digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.UTF_8));
        digest.update((byte) ':');
        digest.update(bytes);
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
