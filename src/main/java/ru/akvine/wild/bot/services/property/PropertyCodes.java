package ru.akvine.wild.bot.services.property;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyCodes {
    public interface ScheduledPropertiesCodes {
        String CHECK_ADVERT_ITERATIONS_BEFORE_INCREASE = "check.advert.iterations.before.increase";
        String CHECK_ADVERT_CRON_MILLISECONDS = "check.advert.cron.milliseconds";
    }

    public interface CustomPropertiesCodes {
        String ADVERT_CPM_INCREASE_VALUE = "advert.cpm.increase.value";
        String MAX_START_SUM_DIFFERENCE = "max.start.sum.difference";
        String MAX_ADVERT_CPM_LIMIT = "advert.max.cpm";
    }

    public interface WildberriesIntegrationPropertiesCodes {
        String WILDBERRIES_WAREHOUSE_ID = "wildberries.warehouse.id";
    }
}
