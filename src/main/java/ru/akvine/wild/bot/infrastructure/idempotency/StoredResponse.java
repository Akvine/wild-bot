package ru.akvine.wild.bot.infrastructure.idempotency;

/**
 * HTTP-ответ, сохранённый для повторных запросов
 *
 * @param status код ответа
 * @param contentType тип содержимого или {@code null}
 * @param bodyBase64 тело ответа в Base64
 */
public record StoredResponse(int status, String contentType, String bodyBase64) {}
