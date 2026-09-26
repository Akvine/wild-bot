package ru.akvine.wild.bot.infrastructure.outbox;

/**
 * Сообщение outbox, взятое relay'ем в обработку
 *
 * @param id идентификатор
 * @param type тип сообщения: по нему выбирается {@link OutboxHandler}
 * @param payload содержимое в виде JSON
 * @param dedupKey ключ дедупликации или {@code null}
 * @param attempts сколько раз отправка уже не удалась
 */
public record OutboxMessage(long id, String type, String payload, String dedupKey, int attempts) {}
