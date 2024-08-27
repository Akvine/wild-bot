package ru.akvine.wild.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.akvine.wild.bot.exceptions.TelegramConfigurationException;
import ru.akvine.wild.bot.telegram.bot.TelegramDevBot;
import ru.akvine.wild.bot.telegram.bot.TelegramDummyBot;
import ru.akvine.wild.bot.telegram.bot.TelegramProductionBot;
import ru.akvine.wild.bot.telegram.filter.MessageFilter;
import ru.akvine.wild.bot.telegram.webhook.dto.GetWebhookInfoResponse;
import ru.akvine.wild.bot.telegram.webhook.dto.GetWebhookRequest;
import ru.akvine.wild.bot.telegram.webhook.dto.SetWebhookRequest;
import ru.akvine.wild.bot.telegram.webhook.dto.SetWebhookResponse;

import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TelegramBotConfig {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    private static final String HTTPS_PROXY_PORT_PROPERTY_NAME = "https.proxyPort";
    private static final String HTTPS_PROXY_HOST_PROPERTY_NAME = "https.proxyHost";


    @Value("${telegram.bot.enabled}")
    private boolean isBotEnabled;
    @Value("${telegram.bot.dev.mode.enabled}")
    private boolean isDevModeEnabled;
    @Value("${telegram.bot.path}")
    private String botPath;
    @Value("${telegram.bot.secret}")
    private String botSecret;
    @Value("${telegram.bot.allowed_updates}")
    private List<String> allowedUpdates;
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String botUsername;

    private final Environment environment;
    private final RestTemplate restTemplate = new RestTemplate();

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
    public TelegramBot telegramBot(DefaultBotOptions defaultBotOptions, MessageFilter startMessageFilter) throws TelegramApiException {
        if (!isBotEnabled) {
            return new TelegramDummyBot();
        }

        if (isDevModeEnabled) {
            TelegramDevBot bot = new TelegramDevBot(defaultBotOptions,
                    botToken,
                    botUsername,
                    startMessageFilter);
            List<BotCommand> listCommands = initBotCommands();
            bot.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            return bot;
        } else {
            TelegramProductionBot bot = new TelegramProductionBot(
                    defaultBotOptions,
                    botToken,
                    startMessageFilter
            );
            bot.setBotUsername(botUsername);
            bot.setBotPath(botPath);
            setWebhook(botToken);
            List<BotCommand> listCommands = initBotCommands();
            bot.execute(new SetMyCommands(listCommands, new BotCommandScopeDefault(), null));
            return bot;
        }
    }

    private void setWebhook(String botToken) {
        GetWebhookInfoResponse getWebhookInfoResponse = sendGetWebhookInfoRequest(botToken);
        String botPathWithSecret = StringUtils.endsWith(botPath, "/")
                ? botPath + botSecret
                : botPath + "/" + botSecret;

        if (!getWebhookInfoResponse.isOk() || !botPathWithSecret.equals(getWebhookInfoResponse.getResult().getUrl())) {
            SetWebhookResponse setWebhookResponse = sendSetWebhookRequest(botToken, botPathWithSecret);
            if (!setWebhookResponse.isOk()) {
                throw new TelegramConfigurationException("Set telegram webhook error");
            }
            getWebhookInfoResponse = sendGetWebhookInfoRequest(botToken);
        }
        if (getWebhookInfoResponse.getResult().isHasCustomCertificate()) {
            logger.warn("Check ssl certificate");
        }
    }

    private GetWebhookInfoResponse sendGetWebhookInfoRequest(String botToken) {
        GetWebhookRequest request = new GetWebhookRequest(TELEGRAM_API_URL, botToken);
        return restTemplate.getForObject(
                request.getTelegramRequestMethod(),
                GetWebhookInfoResponse.class);
    }

    private SetWebhookResponse sendSetWebhookRequest(String botToken, String botPath) {
        SetWebhookRequest request = new SetWebhookRequest(TELEGRAM_API_URL, botToken, botPath);
        request.setAllowedUpdates(allowedUpdates);

        return restTemplate.postForObject(
                request.getTelegramRequestMethod(),
                null,
                SetWebhookResponse.class);
    }

    private List<BotCommand> initBotCommands() {
        return List.of(
                new BotCommand("/start", "Начать работу с ботом"),
                new BotCommand("/help", "Получение списка доступных команд")
        );
    }
}
