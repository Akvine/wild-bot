package ru.akvine.wild.bot.services.property;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PropertyCodes {
    private static final Map<String, Boolean> PROPERTIES_MAP;

    static {
        Map<String, Boolean> map = new HashMap<>();

        for (Class<?> clazz : PropertyCodes.class.getDeclaredClasses()) {
            if (!clazz.isEnum()) {
                continue;
            }

            for (Object constant : clazz.getEnumConstants()) {
                try {
                    Field nameField = clazz.getDeclaredField("name");
                    nameField.setAccessible(true);
                    String name = (String) nameField.get(constant);

                    Field immutableField = clazz.getDeclaredField("immutable");
                    immutableField.setAccessible(true);
                    boolean immutable = immutableField.getBoolean(constant);

                    map.put(name, immutable);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new ExceptionInInitializerError(e);
                }
            }
        }

        PROPERTIES_MAP = Collections.unmodifiableMap(map);
    }

    @Getter
    @AllArgsConstructor
    public enum ScheduledPropertiesCodes {
        CHECK_ADVERT_ITERATIONS_BEFORE_INCREASE("check.advert.iterations.before.increase", false),
        CHECK_ADVERT_CRON_MILLISECONDS("check.advert.cron.milliseconds", true);

        private final String name;
        private final boolean immutable;
    }

    @Getter
    @AllArgsConstructor
    public enum CustomPropertiesCodes {
        CREATE_ADVERTS_BY_API_ENABLED("create.adverts.by.api.enabled", false),
        ADVERT_CPM_INCREASE_VALUE("advert.cpm.increase.value", false),
        ADVERT_MIN_CPM("advert.min.cpm", false),
        ADVERT_BUDGET_SUM_INCREASE_VALUE("advert.budget.sum.increase.value", false),
        ADVERT_SET_AVAILABLE_FOR_NEXT_DAY_ENABLED("advert.set.available.for.next.day.enabled", false),
        MAX_START_SUM_DIFFERENCE("max.start.sum.difference", false),
        MAX_ADVERT_CPM_LIMIT("advert.max.cpm", false),
        QR_CODE_URL("qr.code.url", false);

        private final String name;
        private final boolean immutable;
    }

    @AllArgsConstructor
    @Getter
    public enum WildberriesIntegrationPropertiesCodes {
        WILDBERRIES_WAREHOUSE_ID("wildberries.warehouse.id", false),
        WILDBERRIES_API_TOKEN_VALIDATE_ENABLED("wildberries.api.token.validate.enabled", false),
        WILDBERRIES_API_TOKEN_VALIDATE_PATTERN("wildberries.api.token.validate.pattern", false),
        WILDBERRIES_CHANGE_STOCKS_COUNT_VALUE("wildberries.change.stocks.count", false);

        private final String name;
        private final boolean immutable;
    }

    @AllArgsConstructor
    @Getter
    public enum QRaftIntegrationPropertiesCodes {
        INTEGRATION_ENABLED("qraft.integration.enabled", false),
        ERROR_CORRECTION_LEVEL("qraft.request.param.ecl", false),
        QR_SIZE("qraft.request.param.qr.size", false),
        BORDER_SIZE("qraft.request.param.border.size", false),
        RADIUS_FACTOR("qraft.request.param.radiusFactor", false),
        CORNER_BLOCK_RADIUS_FACTOR("qraft.request.param.cornerBlockRadiusFactor", false),
        ROUND_INNER_CORNERS("qraft.request.param.roundInnerCorners", false),
        ROUND_OUTER_CORNERS("qraft.request.param.roundOuterCorners", false),
        CORNER_BLOCKS_AS_CIRCLES("qraft.request.param.cornerBlocksAsCircles", false),
        IMAGE_TYPE("qraft.request.param.image.type", false);

        private final String name;
        private final boolean immutable;
    }

    public static boolean isImmutable(String key) {
        if (PROPERTIES_MAP.containsKey(key)) {
            return PROPERTIES_MAP.get(key);
        }

        return false;
    }
}
