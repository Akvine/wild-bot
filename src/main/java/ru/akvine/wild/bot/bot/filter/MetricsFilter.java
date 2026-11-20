package ru.akvine.wild.bot.bot.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;

/**
 * Считает обработанные обновления бота и время их обработки: таймер {@value #UPDATES} с метками
 * <ul>
 *     <li>{@code bot} - telegram или max;</li>
 *     <li>{@code type} - message или callback;</li>
 *     <li>{@code outcome} - success, duplicate (повторная доставка, см. {@link DuplicateUpdateFilter}) или error;</li>
 *     <li>{@code exception} - класс исключения, при успехе none.</li>
 * </ul>
 * Место в цепочке - сразу после {@link BotExceptionFilter}: тот превращает исключения в ответ пользователю, и до
 * фильтров перед ним они уже не доходят. В {@code message.filers.list} фильтр указывать не нужно: при включённых
 * метриках ({@code monitoring.metrics.enabled}) его ставит на это место {@code BotFilterConfig}. Указать можно -
 * тогда он останется там, где указан.
 */
@Component
@RequiredArgsConstructor
public class MetricsFilter extends MessageFilter {
    public static final String UPDATES = "bot.updates";

    private static final String SUCCESS = "success";
    private static final String DUPLICATE = "duplicate";
    private static final String ERROR = "error";
    private static final String NO_EXCEPTION = "none";
    private static final String UNKNOWN = "unknown";

    private final MeterRegistry meterRegistry;

    @Override
    public Response handle(Payload payload) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = SUCCESS;
        String exception = NO_EXCEPTION;
        try {
            Response response = nextMessageFilter.handle(payload);
            if (response != null && response.isIgnored()) {
                outcome = DUPLICATE;
            }
            return response;
        } catch (RuntimeException | Error e) {
            outcome = ERROR;
            exception = e.getClass().getSimpleName();
            throw e;
        } finally {
            sample.stop(timer(payload, outcome, exception));
        }
    }

    private Timer timer(Payload payload, String outcome, String exception) {
        BotType botType = payload.getBotType();
        BotDataType dataType = payload.getBotDataType();
        return Timer.builder(UPDATES)
                .description("Bot updates processing time")
                .tag("bot", botType == null ? UNKNOWN : botType.getType())
                .tag("type", dataType == null ? UNKNOWN : dataType.name().toLowerCase(Locale.ROOT))
                .tag("outcome", outcome)
                .tag("exception", exception)
                .register(meterRegistry);
    }
}
