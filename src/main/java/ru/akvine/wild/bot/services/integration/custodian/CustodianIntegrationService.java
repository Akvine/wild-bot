package ru.akvine.wild.bot.services.integration.custodian;

import ru.akvine.wild.bot.services.integration.custodian.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyResponse;

public interface CustodianIntegrationService {
    PropertyResponse getProperties(GetPropertiesRequest request);
}
