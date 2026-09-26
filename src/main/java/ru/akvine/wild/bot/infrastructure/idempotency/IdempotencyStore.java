package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.Duration;
import java.util.Optional;

/**
 * Хранилище ключей идемпотентности. Записи истекают по времени; истёкшая запись считается отсутствующей.
 * Реализации обязаны выполнять {@link #tryBegin} атомарно: из нескольких одновременных вызовов с одним
 * ключом (в том числе с разных инстансов) успешным может быть только один.
 */
public interface IdempotencyStore {

    /**
     * Создаёт запись {@link IdempotencyStatus#IN_PROGRESS}, если такого ключа ещё нет
     *
     * @param inProgressTtl на сколько запись закрепляет ключ, если операция так и не завершится
     *        (например, приложение упало): по истечении ключ снова можно использовать
     * @return {@code true}, если запись создана этим вызовом и операцию нужно выполнять
     */
    boolean tryBegin(String key, String fingerprint, Duration inProgressTtl);

    Optional<IdempotencyRecord> find(String key);

    /**
     * Переводит запись в {@link IdempotencyStatus#COMPLETED} и сохраняет результат
     *
     * @param resultTtl сколько хранить результат для повторных запросов
     */
    void complete(String key, String payload, Duration resultTtl);

    /**
     * Удаляет запись, чтобы тот же ключ можно было использовать заново (операция не удалась)
     */
    void release(String key);
}
