package ru.akvine.marketspace.bot.controller.converters;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.utils.DateUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LaunchedAdvertsConverter {
    @Value("${max.running.adverts.limit}")
    private int maxRunningAdvertsLimit;

    private final static String NEW_LINE = "\n";

    public SendMessage convertToLaunchedListMessage(String chatId,
                                                    int availableTestsCount,
                                                    List<AdvertModel> runningAdverts) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(runningAdverts, "runningAdverts is null");

        StringBuilder sb = new StringBuilder();
        sb.append("Количество доступных тестов: ").append(availableTestsCount).append(NEW_LINE);
        sb.append("Количество запущенных рекламных кампаний: ")
                .append(runningAdverts.size()).append(" / ").append(maxRunningAdvertsLimit).append(NEW_LINE);
        if (!runningAdverts.isEmpty()) {
            sb.append("=======================").append(NEW_LINE);
        }

        runningAdverts.forEach(advert -> {
            sb.append("Advert ID: ").append(advert.getExternalId()).append(NEW_LINE);
            sb.append("Название: ").append(advert.getName()).append(NEW_LINE);
            sb.append("Дата запуска теста по РК: ")
                    .append(DateUtils.formatLocalDateTime(advert.getStartCheckDateTime()))
                    .append(NEW_LINE);
            sb.append("Начальный бюджет: ").append(advert.getStartBudgetSum()).append(" руб.").append(NEW_LINE);
            sb.append("Бюджет на момент проверки РК: ").append(advert.getCheckBudgetSum()).append(" руб.").append(NEW_LINE);
            sb.append("Текущий CPM: ").append(advert.getCpm()).append(NEW_LINE);
            sb.append("Дата следующей проверки: ")
                    .append(DateUtils.formatLocalDateTime(advert.getNextCheckDateTime()))
                    .append(NEW_LINE);
            sb.append("=======================").append(NEW_LINE);
        });
        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return new SendMessage(chatId, sb.toString());
    }
}
