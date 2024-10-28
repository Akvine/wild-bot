package ru.akvine.wild.bot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.akvine.wild.bot.constants.ProxyConstants;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.facades.proxy.WildberriesProxiesFacade;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.proxy.WildberriesIntegrationServiceProxy;

import java.util.Collections;
import java.util.List;

@Configuration
public class ProxyConfig {

    @Bean
    @Primary
    public WildberriesIntegrationService wildberriesIntegrationService(WildberriesProxiesFacade proxies,
                                                                       @Qualifier(ProxyConstants.ORIGIN_BEAN_NAME) WildberriesIntegrationService wildberriesIntegrationService,
                                                                       @Value("${wildberries.proxies}") List<String> enabledProxyTypes) {
        Collections.reverse(enabledProxyTypes);
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
