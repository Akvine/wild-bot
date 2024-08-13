package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.exceptions.telegram.TelegramExceptionHandler;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.ClientSubscriptionService;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.marketspace.bot.telegram.filter.*;

@Configuration
@RequiredArgsConstructor
public class MessageFilterConfig {
    private final MessageDispatcher dispatcher;
    private final ClientService clientService;
    private final ClientSubscriptionService clientSubscriptionService;
    private final TelegramExceptionHandler telegramExceptionHandler;

    @Bean
    public MessageFilter messageFilters() {
        UpdateConverterFilter updateConverterFilter = new UpdateConverterFilter(dispatcher);
        TelegramExceptionFilter exceptionHandlerFilter = new TelegramExceptionFilter(telegramExceptionHandler);
        ClientFilter clientFilter = new ClientFilter(clientService);
        ClientBlockedFilter clientBlockedFilter = new ClientBlockedFilter(clientService);
        ClientSubscriptionFilter clientSubscriptionFilter = new ClientSubscriptionFilter(clientSubscriptionService);
        MDCFilter mdcFilter = new MDCFilter(clientService);

        exceptionHandlerFilter.setNextMessageFilter(clientFilter);

        clientFilter.setNextMessageFilter(mdcFilter);

        mdcFilter.setNextMessageFilter(clientBlockedFilter);

        clientBlockedFilter.setNextMessageFilter(clientSubscriptionFilter);

        clientSubscriptionFilter.setNextMessageFilter(updateConverterFilter);

        return exceptionHandlerFilter;
    }
}

