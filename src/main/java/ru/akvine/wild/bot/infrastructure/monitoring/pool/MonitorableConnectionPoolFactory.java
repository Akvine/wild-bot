package ru.akvine.wild.bot.infrastructure.monitoring.pool;

import static java.util.Arrays.stream;
import static ru.akvine.wild.bot.infrastructure.monitoring.pool.MonitorableConnectionPoolFactory.ClassToMonitorConstructor.of;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Подбирает реализацию {@link MonitorableConnectionPool} по типу {@link DataSource}
 */
@Slf4j
public class MonitorableConnectionPoolFactory {

    // can't use Constructor::new because in that case Java tries to load that class
    // and all it's imports, which exactly what we want to avoid because that Pool Provider may not be
    // on the classpath
    private static final ClassToMonitorConstructor[] constructors = new ClassToMonitorConstructor[] {
        of("com.zaxxer.hikari.HikariDataSource", (dataSource, poolName) -> new HikariMonitorableConnectionPool(
                dataSource, poolName))
    };

    public static MonitorableConnectionPool create(DataSource dataSource, String poolName) {
        return stream(constructors)
                .filter(c -> isInstance(dataSource, c.getDataSourceClassName()))
                .findFirst()
                .map(e -> e.getMonitorConstructor().apply(dataSource, poolName))
                .orElseThrow(() -> unknownDataSourceException(dataSource, poolName));
    }

    private static boolean isInstance(DataSource dataSource, String className) {
        try {
            return Class.forName(className).isInstance(dataSource);
        } catch (ClassNotFoundException e) {
            logger.trace("Class of type [{}] is not on classpath", className);
            return false;
        } catch (Exception ex) {
            logger.warn("Class of type [{}] can't be loaded", className, ex);
            return false;
        }
    }

    private static IllegalArgumentException unknownDataSourceException(DataSource dataSource, String poolName) {
        String supportableDataSourceClasses = stream(constructors)
                .map(ClassToMonitorConstructor::getDataSourceClassName)
                .collect(Collectors.joining(","));

        return new IllegalArgumentException(String.format(
                "Can't enable Connection Pool Monitor [%s]. DataSource is of type [%s] but only [%s] are supported.",
                poolName, dataSource.getClass(), supportableDataSourceClasses));
    }

    static class ClassToMonitorConstructor {
        private final String dataSourceClassName;
        private final BiFunction<DataSource, String, MonitorableConnectionPool> monitorConstructor;

        private ClassToMonitorConstructor(
                String dataSourceClassName,
                BiFunction<DataSource, String, MonitorableConnectionPool> monitorConstructor) {
            this.dataSourceClassName = Objects.requireNonNull(dataSourceClassName, "dataSourceClassName");
            this.monitorConstructor = Objects.requireNonNull(monitorConstructor, "monitorConstructor");
        }

        static ClassToMonitorConstructor of(
                String className, BiFunction<DataSource, String, MonitorableConnectionPool> constructor) {
            return new ClassToMonitorConstructor(className, constructor);
        }

        String getDataSourceClassName() {
            return dataSourceClassName;
        }

        BiFunction<DataSource, String, MonitorableConnectionPool> getMonitorConstructor() {
            return monitorConstructor;
        }
    }
}
