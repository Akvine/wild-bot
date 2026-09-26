package ru.akvine.wild.bot.infrastructure.monitoring.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

/**
 * Считает запросы, подходящие под регулярное выражение (без учёта регистра, {@code .} захватывает
 * и переводы строк), внутри метрики потока. Запросы, выполняемые из перечисленных классов
 * ({@code excludeClasses}, ищутся по стеку вызовов), не считаются.
 */
public class DbMetricPerQuery {
    private final String mnemonic;
    private final String queryRegex;
    private final Pattern queryPattern;
    private final List<String> excludeClasses = new CopyOnWriteArrayList<>();
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicLong queriesCount = new AtomicLong(0);

    public DbMetricPerQuery(String mnemonic, String queryRegex) {
        this.mnemonic = Objects.requireNonNull(mnemonic, "mnemonic must be present");
        this.queryRegex = Objects.requireNonNull(queryRegex, "queryRegex must be present");
        this.queryPattern = Pattern.compile(queryRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void resetQueriesCount() {
        queriesCount.set(0);
    }

    public long getQueriesCount() {
        return queriesCount.get();
    }

    public void incrementQueriesCount() {
        queriesCount.incrementAndGet();
    }

    public String getQueryRegex() {
        return queryRegex;
    }

    public boolean matches(String query) {
        return queryPattern.matcher(query).matches();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public DbMetricPerQuery setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        return this;
    }

    public List<String> getExcludeClasses() {
        return new ArrayList<>(excludeClasses);
    }

    /**
     * @param excludeClasses имена (или части имён) классов через запятую
     */
    public DbMetricPerQuery parseAndAddExcludeClasses(String excludeClasses) {
        if (!StringUtils.hasText(excludeClasses)) {
            return this;
        }
        this.excludeClasses.addAll(Arrays.asList(excludeClasses.replaceAll(" ", "").split(",")));
        return this;
    }

    public DbMetricPerQuery addExcludeClass(String excludeClass) {
        if (StringUtils.hasText(excludeClass)) {
            this.excludeClasses.add(excludeClass);
        }
        return this;
    }
}
