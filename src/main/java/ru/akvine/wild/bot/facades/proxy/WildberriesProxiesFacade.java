package ru.akvine.wild.bot.facades.proxy;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.services.integration.wildberries.proxy.WildberriesIntegrationServiceProxy;

@Getter
@AllArgsConstructor
public class WildberriesProxiesFacade {
    private final Map<ProxyType, WildberriesIntegrationServiceProxy> map;
}
