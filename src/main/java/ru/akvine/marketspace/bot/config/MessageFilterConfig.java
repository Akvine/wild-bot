package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.marketspace.bot.telegram.filter.MessageExceptionFilter;
import ru.akvine.marketspace.bot.telegram.filter.MessageFilter;
import ru.akvine.marketspace.bot.telegram.filter.UpdateConverterFilter;

@Configuration
@RequiredArgsConstructor
public class MessageFilterConfig {
    private final MessageDispatcher dispatcher;

    @Bean
    public MessageFilter messageFilters() {
        UpdateConverterFilter updateConverterFilter = new UpdateConverterFilter(dispatcher);
        MessageFilter exceptionHandlerFilter = new MessageExceptionFilter();
        exceptionHandlerFilter.setMessageFilter(updateConverterFilter);
        return exceptionHandlerFilter;
    }
}

