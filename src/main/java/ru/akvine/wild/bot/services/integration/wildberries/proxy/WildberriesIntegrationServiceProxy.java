package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import lombok.Setter;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;

@Setter
public abstract class WildberriesIntegrationServiceProxy implements WildberriesIntegrationService {
    protected WildberriesIntegrationService targetObject;

    public abstract ProxyType getType();

}
