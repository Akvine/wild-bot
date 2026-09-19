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
        String ADVERT_SET_AVAILABLE_FOR_NEXT_DAY_ENABLED = "advert.set.available.for.next.day.enabled";
        String MAX_START_SUM_DIFFERENCE = "max.start.sum.difference";
        String MAX_ADVERT_CPM_LIMIT = "advert.max.cpm";
        String QR_CODE_URL = "qr.code.url";
    }

    public interface WildberriesIntegrationPropertiesCodes {
        String WILDBERRIES_WAREHOUSE_ID = "wildberries.warehouse.id";
        String WILDBERRIES_API_TOKEN_VALIDATE_ENABLED = "wildberries.api.token.validate.enabled";
        String WILDBERRIES_API_TOKEN_VALIDATE_PATTERN = "wildberries.api.token.validate.pattern";
    }

    public interface QRaftIntegrationPropertiesCodes {
        String INTEGRATION_ENABLED = "qraft.integration.enabled";
        String ERROR_CORRECTION_LEVEL = "qraft.request.param.ecl";
        String QR_SIZE = "qraft.request.param.qr.size";
        String BORDER_SIZE = "qraft.request.param.border.size";
        String RADIUS_FACTOR = "qraft.request.param.radiusFactor";
        String CORNER_BLOCK_RADIUS_FACTOR = "qraft.request.param.cornerBlockRadiusFactor";
        String ROUND_INNER_CORNERS = "qraft.request.param.roundInnerCorners";
        String ROUND_OUTER_CORNERS = "qraft.request.param.roundOuterCorners";
        String CORNER_BLOCKS_AS_CIRCLES = "qraft.request.param.cornerBlocksAsCircles";
        String IMAGE_TYPE = "qraft.request.param.image.type";
    }
}
