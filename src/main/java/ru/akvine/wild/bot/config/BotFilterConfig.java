package ru.akvine.wild.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.bot.filter.*;
import ru.akvine.wild.bot.exceptions.telegram.BotExceptionHandlerImpl;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.bot.MessageDispatcher;

@Configuration
@RequiredArgsConstructor
public class BotFilterConfig {
    private final MessageDispatcher dispatcher;
    private final ClientService clientService;
    private final BotExceptionHandlerImpl botExceptionHandlerImpl;

    @Bean
    public MessageFilter messageFilters() {
        UserBadMessageFilter userBadMessageFilter = new UserBadMessageFilter(dispatcher);
        BotExceptionFilter exceptionHandlerFilter = new BotExceptionFilter(botExceptionHandlerImpl);
        ClientFilter clientFilter = new ClientFilter(clientService);
        ClientBlockedFilter clientBlockedFilter = new ClientBlockedFilter(clientService);
        WhitelistFilter whitelistFilter = new WhitelistFilter(clientService);
        MDCFilter mdcFilter = new MDCFilter(clientService);

        exceptionHandlerFilter.setNextMessageFilter(clientFilter);

        clientFilter.setNextMessageFilter(mdcFilter);

        mdcFilter.setNextMessageFilter(clientBlockedFilter);

        clientBlockedFilter.setNextMessageFilter(whitelistFilter);

        whitelistFilter.setNextMessageFilter(userBadMessageFilter);

        return exceptionHandlerFilter;
    }
}

