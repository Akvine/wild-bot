package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.bot.filter.MessageFilter;

@Getter
@AllArgsConstructor
public class MessageFiltersFacade {
    private final Map<String, MessageFilter> map;

    public MessageFilter get(String messageFilterClassName) {
        if (map.containsKey(messageFilterClassName)) {
            return map.get(messageFilterClassName);
        }

        throw new IllegalArgumentException(
                "Message filter with name = [" + messageFilterClassName + "] is not supported byu app!");
    }
}
