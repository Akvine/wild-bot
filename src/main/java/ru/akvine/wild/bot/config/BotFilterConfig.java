package ru.akvine.wild.bot.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.bot.filter.MessageHandlerFilter;
import ru.akvine.wild.bot.facades.MessageFiltersFacade;

@Configuration
@RequiredArgsConstructor
public class BotFilterConfig {
    private static final Logger log = LoggerFactory.getLogger(BotFilterConfig.class);

    @Bean
    @Primary
    public InitMessageFilter messageFilter(
            @Value("${message.filers.list}") List<String> messageFilters, MessageFiltersFacade facade) {
        if (CollectionUtils.isEmpty(messageFilters)) {
            return facade.get(MessageHandlerFilter.class.getSimpleName());
        }

        if (!messageFilters.contains(MessageHandlerFilter.class.getSimpleName())) {
            log.warn(
                    "{message.filters.list} property doesn't contain required [{}] filter. Put required filter at the end of the list",
                    MessageHandlerFilter.class.getSimpleName());
            messageFilters.add(MessageHandlerFilter.class.getSimpleName());
        }

        int lastMessageFilterIndex = messageFilters.size() - 1;
        for (int i = 0; i < messageFilters.size(); ++i) {
            if (i == lastMessageFilterIndex) {
                break;
            }

            MessageFilter firstFilter = facade.get(messageFilters.get(i));
            MessageFilter secondFilter = facade.get(messageFilters.get(i + 1));
            firstFilter.setNextMessageFilter(secondFilter);
        }

        log.info("Initialized message filters in next order: {}", messageFilters);
        return facade.get(messageFilters.getFirst());
    }
}
