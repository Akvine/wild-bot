package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public MessageFilter messageFilters() {
        UpdateConverterFilter updateConverterFilter = new UpdateConverterFilter(dispatcher);
        MessageExceptionFilter exceptionHandlerFilter = new MessageExceptionFilter();
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

