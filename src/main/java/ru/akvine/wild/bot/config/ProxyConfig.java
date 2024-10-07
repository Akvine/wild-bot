package ru.akvine.wild.bot.config;

import jakarta.annotation.Priority;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.facades.proxy.WildberriesProxiesFacade;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.proxy.WildberriesIntegrationServiceProxy;

import java.util.List;

@Configuration
public class ProxyConfig {

    @Bean
    @Primary
    public WildberriesIntegrationService wildberriesIntegrationService(WildberriesProxiesFacade proxies,
                                                                       @Qualifier("origin") WildberriesIntegrationService wildberriesIntegrationService,
                                                                       @Value("${wildberries.proxies}") List<String> enabledProxyTypes) {
        WildberriesIntegrationService targetObject = wildberriesIntegrationService;
        for (String enabledProxyType : enabledProxyTypes) {
            ProxyType type = ProxyType.safeValueOf(enabledProxyType);
            WildberriesIntegrationServiceProxy proxy = proxies.getMap().get(type);
            proxy.setTargetObject(targetObject);
            targetObject = proxy;
        }

        return targetObject;
    }
}
