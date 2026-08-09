package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.bot.InvalidBotKeyboardFactory;

@Getter
@AllArgsConstructor
public class BotKeyboardFactoryFacade {
    private final Map<String, BotKeyboardFactory> factories;

    public BotKeyboardFactory resolve(BotType botType, ClientState clientState) {
        String factoryId = BotKeyboardFactory.getUniqueIdentifier(botType, clientState);
        if (factories.containsKey(factoryId)) {
            return factories.get(factoryId);
        }

        String errorMessage = String.format(
                "Can't resolve bot key board factory for identifier = [%s]. Bot type = [%s], state = [%s]",
                BotKeyboardFactory.getUniqueIdentifier(botType, clientState), botType, clientState);
        throw new InvalidBotKeyboardFactory(errorMessage);
    }
}
