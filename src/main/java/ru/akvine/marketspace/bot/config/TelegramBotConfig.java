package ru.akvine.marketspace.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.akvine.marketspace.bot.telegram.TelegramLongPoolingBot;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;

import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig {
    private static final String TELEGRAM_API_URL = "https://api.bot.org/bot";

    private static final String HTTPS_PROXY_PORT_PROPERTY_NAME = "https.proxyPort";
    private static final String HTTPS_PROXY_HOST_PROPERTY_NAME = "https.proxyHost";


    @Value("${telegram.bot.enabled}")
    private boolean enabled;
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    private final Environment environment;

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        DefaultBotOptions defaultBotOptions = new DefaultBotOptions();

        if (environment.getProperty(HTTPS_PROXY_PORT_PROPERTY_NAME) == null || environment.getProperty(HTTPS_PROXY_HOST_PROPERTY_NAME) == null) {
            return defaultBotOptions;
        }
        String proxyHost = environment.getProperty(HTTPS_PROXY_HOST_PROPERTY_NAME);
        int proxyPort = Integer.parseInt(Objects.requireNonNull(environment.getProperty(HTTPS_PROXY_PORT_PROPERTY_NAME)));
        DefaultBotOptions.ProxyType proxyType = DefaultBotOptions.ProxyType.HTTP;

        defaultBotOptions.setProxyType(proxyType);
        defaultBotOptions.setProxyHost(proxyHost);
        defaultBotOptions.setProxyPort(proxyPort);
        return defaultBotOptions;
    }

    @Bean
    public TelegramLongPoolingBot telegramLongPollingBot(DefaultBotOptions defaultBotOptions, MessageDispatcher messageDispatcher) throws TelegramApiException {
        TelegramLongPoolingBot bot = new TelegramLongPoolingBot(defaultBotOptions,
                botToken,
                botUsername,
                messageDispatcher);
        List<BotCommand> listCommands = initBotCommands();
        bot.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    private List<BotCommand> initBotCommands() {
        return List.of(
                new BotCommand("/start", "Запустить случайную рекламную кампанию"),
                new BotCommand("/statistic", "Получить статистику по запущенным рекламным кампаниям"),
                new BotCommand("/report", "Сгенерировать отчет о тестах"),
                new BotCommand("/help", "Получение списка доступных команд"),
                new BotCommand("/stop", "Отменить запуск рекламной кампании")
        );
    }
}
