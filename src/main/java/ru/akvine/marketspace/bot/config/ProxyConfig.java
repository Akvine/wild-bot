package ru.akvine.marketspace.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcherOrigin;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcherProxy;

@Configuration
public class ProxyConfig {

    @Bean
    public MessageDispatcher messageDispatcher(MessageDispatcherOrigin messageDispatcher) {
        return new MessageDispatcherProxy(messageDispatcher);
    }
}
