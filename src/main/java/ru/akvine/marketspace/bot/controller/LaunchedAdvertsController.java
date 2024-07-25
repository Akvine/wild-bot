package ru.akvine.marketspace.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.converters.LaunchedAdvertsConverter;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LaunchedAdvertsController {
    private final LaunchedAdvertsConverter launchedAdvertsConverter;
    private final AdvertService advertService;

    public SendMessage getLaunchedList(String chatId) {
        List<AdvertBean> runningAdverts = advertService.getAdvertsByChatIdAndStatuses(chatId, List.of(AdvertStatus.RUNNING));
        return launchedAdvertsConverter.convertToLaunchedListMessage(chatId, runningAdverts);
    }
}
