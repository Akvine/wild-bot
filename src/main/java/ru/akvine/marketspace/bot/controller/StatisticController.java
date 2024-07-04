package ru.akvine.marketspace.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.converters.StatisticConverter;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticConverter statisticConverter;
    private final AdvertService advertService;

    public SendMessage getStatistic(String chatId) {
        List<AdvertBean> runningAdverts = advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING));
        return statisticConverter.convertToStatistic(chatId, runningAdverts);
    }
}
