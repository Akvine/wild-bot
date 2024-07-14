package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.marketspace.bot.telegram.filter.*;

@Configuration
@RequiredArgsConstructor
public class MessageFilterConfig {
    private final MessageDispatcher dispatcher;
    private final ClientService clientService;

    @Bean
    public MessageFilter messageFilters() {
        UpdateConverterFilter updateConverterFilter = new UpdateConverterFilter(dispatcher);
        MessageExceptionFilter exceptionHandlerFilter = new MessageExceptionFilter();
        ClientFilter clientFilter = new ClientFilter(clientService);
        ClientBlockedFilter clientBlockedFilter = new ClientBlockedFilter(clientService);
        ClientWhitelistFilter clientWhitelistFilter = new ClientWhitelistFilter(clientService);

        exceptionHandlerFilter.setNextMessageFilter(clientFilter);

        clientFilter.setNextMessageFilter(clientBlockedFilter);

        clientBlockedFilter.setNextMessageFilter(clientWhitelistFilter);

        clientWhitelistFilter.setNextMessageFilter(updateConverterFilter);

        return exceptionHandlerFilter;
    }
}

