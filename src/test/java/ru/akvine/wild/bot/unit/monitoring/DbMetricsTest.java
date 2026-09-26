package ru.akvine.wild.bot.unit.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.infrastructure.monitoring.db.DbInfoConnection;
import ru.akvine.wild.bot.infrastructure.monitoring.db.DbMetricsConfigurer;
import ru.akvine.wild.bot.infrastructure.monitoring.db.DbMetricsListener;
import ru.akvine.wild.bot.infrastructure.monitoring.db.DbMetricsService;
import ru.akvine.wild.bot.infrastructure.monitoring.db.JdbcUrlParser;
import ru.akvine.wild.bot.infrastructure.monitoring.db.JmxMetrics;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.MissConfigurationException;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.QueryMetricNotFound;
import ru.akvine.wild.bot.infrastructure.monitoring.db.exception.ThreadMetricNotFound;

class DbMetricsTest {

    private Map<String, String> workerProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("enabled", "true");
        properties.put("thread.metrics", "worker");
        properties.put("worker.thread.name.regex", "worker-.*");
        properties.put("worker.enabled", "true");
        properties.put("worker.query.metrics", "writes");
        properties.put("worker.writes.regex", "(insert|update|delete).*");
        properties.put("worker.writes.enabled", "true");
        return properties;
    }

    private void runInThread(String name, Runnable task) throws InterruptedException {
        Thread thread = new Thread(task, name);
        thread.start();
        thread.join();
    }

    @Test
    @DisplayName("Коммиты и запросы считаются только у потоков, чьё имя подходит под регулярное выражение")
    void countsOnlyMatchingThreads() throws InterruptedException {
        DbMetricsService service = DbMetricsConfigurer.configure(workerProperties(), null);
        DbMetricsListener listener = new DbMetricsListener(service.getMetrics());

        runInThread("worker-1", () -> {
            listener.onQueryExecuted("UPDATE clients\nSET name = 'x'", 0);
            listener.onQueryExecuted("select * from clients", 0);
            listener.onCommit();
        });
        listener.onQueryExecuted("delete from clients", 0); // основной поток: не worker-*
        listener.onCommit();

        assertThat(service.getQueryCount("worker", "writes")).isEqualTo(1);
        assertThat(service.getCommitCount("worker")).isEqualTo(1);
        assertThat(service.getTotalCommitCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Выключенные метрики ничего не считают, включение на лету сразу работает")
    void disabledMetricsCountNothingAndCanBeEnabledAtRuntime() throws InterruptedException {
        Map<String, String> properties = workerProperties();
        properties.put("enabled", "false");
        DbMetricsService service = DbMetricsConfigurer.configure(properties, null);
        DbMetricsListener listener = new DbMetricsListener(service.getMetrics());

        runInThread("worker-1", listener::onCommit);
        assertThat(service.getTotalCommitCount()).isZero();

        service.setEnabled(true);
        runInThread("worker-1", listener::onCommit);
        assertThat(service.getTotalCommitCount()).isEqualTo(1);
        assertThat(service.getCommitCount("worker")).isEqualTo(1);
    }

    @Test
    @DisplayName("Запросы, выполненные из исключённых классов, не считаются")
    void excludedClassesAreNotCounted() throws InterruptedException {
        Map<String, String> properties = workerProperties();
        properties.put("worker.writes.exclude.classes", "DbMetricsTest");
        DbMetricsService service = DbMetricsConfigurer.configure(properties, null);
        DbMetricsListener listener = new DbMetricsListener(service.getMetrics());

        runInThread("worker-1", () -> listener.onQueryExecuted("update clients set a = 1", 0));

        assertThat(service.getQueryCount("worker", "writes")).isZero();
    }

    @Test
    @DisplayName("Общий счётчик коммитов дублируется в JMX-счётчик и сбрасывается вместе с ним")
    void totalCommitsAreMirroredToJmxCounter() {
        Map<String, String> properties = workerProperties();
        properties.put("jmx.commit.metric.enabled", "true");
        properties.put("jmx.commit.metric.name", "totalCommits");
        DbMetricsService service = DbMetricsConfigurer.configure(properties, new JmxMetrics("test.db"));
        DbMetricsListener listener = new DbMetricsListener(service.getMetrics());

        listener.onCommit();
        listener.onCommit();
        assertThat(service.getJmxTotalCommitMetric().getCount()).isEqualTo(2);

        service.resetTotalCommitCount();
        assertThat(service.getTotalCommitCount()).isZero();
        assertThat(service.getJmxTotalCommitMetric().getCount()).isZero();
    }

    @Test
    @DisplayName("Ошибки конфигурации: нет регулярного выражения, нет JMX-реестра")
    void invalidConfigurationIsRejected() {
        Map<String, String> noRegex = workerProperties();
        noRegex.remove("worker.thread.name.regex");
        assertThatThrownBy(() -> DbMetricsConfigurer.configure(noRegex, null))
                .isInstanceOf(MissConfigurationException.class);

        Map<String, String> noQueryRegex = workerProperties();
        noQueryRegex.remove("worker.writes.regex");
        assertThatThrownBy(() -> DbMetricsConfigurer.configure(noQueryRegex, null))
                .isInstanceOf(MissConfigurationException.class);

        Map<String, String> noJmx = workerProperties();
        noJmx.put("jmx.commit.metric.enabled", "true");
        noJmx.put("jmx.commit.metric.name", "totalCommits");
        assertThatThrownBy(() -> DbMetricsConfigurer.configure(noJmx, null))
                .isInstanceOf(MissConfigurationException.class);
    }

    @Test
    @DisplayName("Обращение к несуществующей метрике - понятное исключение")
    void unknownMetricsThrow() {
        DbMetricsService service = DbMetricsConfigurer.configure(workerProperties(), null);

        assertThatThrownBy(() -> service.getCommitCount("nope")).isInstanceOf(ThreadMetricNotFound.class);
        assertThatThrownBy(() -> service.getQueryCount("worker", "nope")).isInstanceOf(QueryMetricNotFound.class);
    }

    @Test
    @DisplayName("JdbcUrlParser достаёт хост, порт и имя базы; для встроенной базы возвращает null")
    void parsesJdbcUrl() {
        JdbcUrlParser parser = new JdbcUrlParser();

        DbInfoConnection postgres =
                parser.parseUrl("jdbc:postgresql://db.local:5432/wild?sslmode=disable", "wild_user");

        assertThat(postgres.getHost()).isEqualTo("db.local");
        assertThat(postgres.getPort()).isEqualTo("5432");
        assertThat(postgres.getServiceName()).isEqualTo("wild");
        assertThat(postgres.getSchema()).isEqualTo("wild_user");
        assertThat(parser.parseUrl("jdbc:h2:mem:testdb;MODE=PostgreSQL", "sa")).isNull();
    }
}
