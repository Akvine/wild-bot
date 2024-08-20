package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.exceptions.telegram.TelegramExceptionHandler;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.marketspace.bot.telegram.filter.*;

@Configuration
@RequiredArgsConstructor
public class TelegramFilterConfig {
    private final MessageDispatcher dispatcher;
    private final ClientService clientService;
    private final TelegramExceptionHandler telegramExceptionHandler;

    @Bean
    public MessageFilter messageFilters() {
        UpdateConverterFilter updateConverterFilter = new UpdateConverterFilter(dispatcher);
        TelegramExceptionFilter exceptionHandlerFilter = new TelegramExceptionFilter(telegramExceptionHandler);
        ClientFilter clientFilter = new ClientFilter(clientService);
        ClientBlockedFilter clientBlockedFilter = new ClientBlockedFilter(clientService);
        WhitelistFilter whitelistFilter = new WhitelistFilter(clientService);
        MDCFilter mdcFilter = new MDCFilter(clientService);

        exceptionHandlerFilter.setNextMessageFilter(clientFilter);

        clientFilter.setNextMessageFilter(mdcFilter);

        mdcFilter.setNextMessageFilter(clientBlockedFilter);

        clientBlockedFilter.setNextMessageFilter(whitelistFilter);

        whitelistFilter.setNextMessageFilter(updateConverterFilter);

        return exceptionHandlerFilter;
    }
}

