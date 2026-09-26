package ru.akvine.wild.bot.unit.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.DuplicateUpdateFilter;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyPayloadSerializer;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyProperties;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyService;
import ru.akvine.wild.bot.infrastructure.idempotency.InMemoryIdempotencyStore;

class DuplicateUpdateFilterTest {
    private final AtomicInteger processed = new AtomicInteger();
    private boolean failNext;

    private final DuplicateUpdateFilter filter = new DuplicateUpdateFilter(
            new IdempotencyService(new InMemoryIdempotencyStore(), new IdempotencyPayloadSerializer()),
            new IdempotencyProperties());

    {
        filter.setNextMessageFilter(new MessageFilter() {
            @Override
            public Response handle(Payload payload) {
                processed.incrementAndGet();
                if (failNext) {
                    throw new IllegalStateException("boom");
                }
                return new Response(payload.getChatId(), "answer", payload.getBotType());
            }
        });
    }

    private Payload payload(String updateId, BotType botType) {
        return new Payload().setChatId("100").setBotType(botType).setUpdateId(updateId);
    }

    @Test
    @DisplayName("Повторная доставка обновления игнорируется: обработчик вызывается один раз")
    void duplicateUpdateIsIgnored() {
        Response first = filter.handle(payload("42", BotType.TELEGRAM));
        Response second = filter.handle(payload("42", BotType.TELEGRAM));

        assertThat(processed).hasValue(1);
        assertThat(first.isIgnored()).isFalse();
        assertThat(first.getText()).isEqualTo("answer");
        assertThat(second.isIgnored()).isTrue();
        assertThat(second.getChatId()).isEqualTo("100");
    }

    @Test
    @DisplayName("Разные обновления и одинаковый id в разных ботах не считаются дублями")
    void differentUpdatesAreProcessed() {
        filter.handle(payload("42", BotType.TELEGRAM));
        filter.handle(payload("43", BotType.TELEGRAM));
        filter.handle(payload("42", BotType.MAX));

        assertThat(processed).hasValue(3);
    }

    @Test
    @DisplayName("Обновление без идентификатора обрабатывается всегда")
    void updateWithoutIdIsAlwaysProcessed() {
        filter.handle(payload(null, BotType.MAX));
        filter.handle(payload(null, BotType.MAX));

        assertThat(processed).hasValue(2);
    }

    @Test
    @DisplayName("Если обработка упала, обновление освобождается и повторная доставка обрабатывается заново")
    void failedUpdateCanBeRedelivered() {
        failNext = true;
        assertThatThrownBy(() -> filter.handle(payload("42", BotType.TELEGRAM)))
                .isInstanceOf(IllegalStateException.class);

        failNext = false;
        Response redelivered = filter.handle(payload("42", BotType.TELEGRAM));

        assertThat(processed).hasValue(2);
        assertThat(redelivered.isIgnored()).isFalse();
    }
}
