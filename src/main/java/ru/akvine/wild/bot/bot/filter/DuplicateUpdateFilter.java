package ru.akvine.wild.bot.bot.filter;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.infrastructure.idempotency.Fingerprints;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyProperties;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService.BeginResult;

/**
 * Отбрасывает повторную доставку уже обработанного (или обрабатываемого сейчас) обновления бота.
 * Telegram присылает обновление повторно, если не получил ответ вовремя, и без этого фильтра клиент
 * получил бы повторные сообщения и повторное выполнение действий (например, запуск кампании).
 * <p>
 * Включается добавлением {@code DuplicateUpdateFilter} в {@code message.filers.list} - сразу после
 * {@code BotExceptionFilter}. Если во время обработки было исключение, обновление освобождается и
 * его повторная доставка будет обработана заново. Обновления без идентификатора пропускаются.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DuplicateUpdateFilter extends MessageFilter {
    private final IdempotencyService idempotencyService;
    private final IdempotencyProperties idempotencyProperties;

    @Override
    public Response handle(Payload payload) {
        if (payload.getUpdateId() == null) {
            return nextMessageFilter.handle(payload);
        }

        IdempotencyProperties.Bot properties = idempotencyProperties.getBot();
        String key = "update:" + payload.getBotType() + ":" + payload.getUpdateId();
        BeginResult begin = idempotencyService.begin(
                key, Fingerprints.of(key), Duration.ofSeconds(properties.getInProgressTtlSeconds()));
        if (begin.outcome() != IdempotencyService.Outcome.STARTED) {
            logger.info(
                    "Duplicate update [{}] for chat id = {}, bot type = {} is ignored ({})",
                    payload.getUpdateId(),
                    payload.getChatId(),
                    payload.getBotType(),
                    begin.outcome());
            return Response.ignored(payload.getChatId(), payload.getBotType());
        }

        try {
            Response response = nextMessageFilter.handle(payload);
            idempotencyService.complete(key, "processed", Duration.ofHours(properties.getTtlHours()));
            return response;
        } catch (RuntimeException | Error e) {
            idempotencyService.release(key);
            throw e;
        }
    }
}
