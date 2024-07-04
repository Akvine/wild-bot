package ru.akvine.marketspace.bot.controller.converters;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatisticConverter {
    @Value("${max.running.adverts.limit}")
    private int maxRunningAdvertsLimit;

    private final static String NEW_LINE = "\n";

    public SendMessage convertToStatistic(String chatId, List<AdvertBean> runningAdverts) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(runningAdverts, "runningAdverts is null");

        StringBuilder sb = new StringBuilder();
        sb.append("=====[Статистика]======").append(NEW_LINE);
        sb.append("Количество запущенных рекламных кампаний: ").append(runningAdverts.size()).append(NEW_LINE);
        sb.append("Максимальное количество возможных запущенных кампаний: ").append(maxRunningAdvertsLimit).append(NEW_LINE);
        sb.append("=======================").append(NEW_LINE);

        runningAdverts.forEach(advert -> {
            sb.append("Advert ID: ").append(advert.getAdvertId()).append(NEW_LINE);
            sb.append("Название: ").append(advert.getName()).append(NEW_LINE);
            sb.append("Дата запуска РК: ").append(advert.getStartCheckDateTime()).append(NEW_LINE);
            sb.append("Начальный бюджет: ").append(advert.getStartBudgetSum()).append(NEW_LINE);
            sb.append("Бюджет на момент проверки РК: ").append(advert.getCheckBudgetSum()).append(NEW_LINE);
            sb.append("CPM: ").append(advert.getCpm()).append(NEW_LINE);
            sb.append("Дата следующей проверки: ").append(advert.getNextCheckDateTime()).append(NEW_LINE);
            sb.append("=======================").append(NEW_LINE);
        });
        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return new SendMessage(chatId, sb.toString());
    }
}
