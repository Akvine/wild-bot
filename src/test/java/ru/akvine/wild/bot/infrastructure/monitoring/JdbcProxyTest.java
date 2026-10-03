package ru.akvine.wild.bot.infrastructure.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

@DisplayName("JDBC-прокси мониторинга")
class JdbcProxyTest {
    private final List<String> queries = new ArrayList<>();
    private final AtomicInteger commits = new AtomicInteger();
    private final SqlExecutionListener listener = new SqlExecutionListener() {
        @Override
        public void onQueryExecuted(String sql, long startedAtNanos) {
            queries.add(sql);
        }

        @Override
        public void onCommit() {
            commits.incrementAndGet();
        }
    };
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:proxytest" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource = new MonitoringDataSourceProxy(h2, listener);
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("create table t (id int primary key, name varchar(20))");
        }
        queries.clear();
    }

    @Test
    @DisplayName("Statement: каждый execute* сообщает слушателю текст запроса")
    void statementExecutionIsReported() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            assertThat(statement.executeUpdate("insert into t values (1, 'a')")).isEqualTo(1);
            assertThat(statement.execute("insert into t values (2, 'b')")).isFalse();
            try (ResultSet rs = statement.executeQuery("select count(*) from t")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(2);
            }
            assertThat(statement.executeLargeUpdate("update t set name = 'z' where id = 1"))
                    .isEqualTo(1L);
            assertThat(statement.executeUpdate("update t set name = 'y' where id = 2", Statement.NO_GENERATED_KEYS))
                    .isEqualTo(1);
            assertThat(statement.execute("select 1", Statement.NO_GENERATED_KEYS))
                    .isTrue();
            assertThat(statement.executeLargeUpdate("update t set name = 'q'", Statement.NO_GENERATED_KEYS))
                    .isEqualTo(2L);
            assertThat(statement.executeUpdate("update t set name = 'w'", new int[] {1}))
                    .isEqualTo(2);
            assertThat(statement.executeUpdate("update t set name = 'e'", new String[] {"ID"}))
                    .isEqualTo(2);
            assertThat(statement.execute("select 2", new int[] {1})).isTrue();
            assertThat(statement.execute("select 3", new String[] {"ID"})).isTrue();
            assertThat(statement.executeLargeUpdate("update t set name = 'r'", new int[] {1}))
                    .isEqualTo(2L);
            assertThat(statement.executeLargeUpdate("update t set name = 't'", new String[] {"ID"}))
                    .isEqualTo(2L);
        }

        assertThat(queries)
                .contains("insert into t values (1, 'a')", "select count(*) from t", "select 1", "select 3")
                .hasSize(13);
    }

    @Test
    @DisplayName("Ошибка запроса тоже сообщается слушателю и пробрасывается дальше")
    void failedQueryIsReported() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.executeQuery("select * from missing_table"))
                    .isInstanceOf(SQLException.class);
        }

        assertThat(queries).containsExactly("select * from missing_table");
    }

    @Test
    @DisplayName("Пачка Statement: запросы собираются в одну метку, а после выполнения или очистки список сбрасывается")
    void statementBatch() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.addBatch("insert into t values (10, 'a')");
            statement.addBatch("insert into t values (11, 'b')");
            assertThat(statement.executeBatch()).hasSize(2);
            statement.addBatch("insert into t values (12, 'c')");
            statement.clearBatch();
            statement.addBatch("insert into t values (13, 'd')");
            assertThat(statement.executeLargeBatch()).hasSize(1);
        }

        assertThat(queries).hasSize(2);
        assertThat(queries.get(0)).startsWith("batch [").contains("(10, 'a')").contains("(11, 'b')");
        assertThat(queries.get(1)).contains("(13, 'd')").doesNotContain("(12, 'c')");
    }

    @Test
    @DisplayName("PreparedStatement: execute*, пачка и параметры")
    void preparedStatement() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String insert = "insert into t values (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setInt(1, 1);
                ps.setString(2, "a");
                assertThat(ps.executeUpdate()).isEqualTo(1);
                ps.setInt(1, 2);
                ps.setString(2, "b");
                assertThat(ps.executeLargeUpdate()).isEqualTo(1L);
                ps.setInt(1, 3);
                ps.setNull(2, java.sql.Types.VARCHAR);
                assertThat(ps.execute()).isFalse();
                ps.setInt(1, 4);
                ps.setObject(2, "d");
                ps.addBatch();
                ps.setInt(1, 5);
                ps.setObject(2, "e", java.sql.Types.VARCHAR);
                ps.addBatch();
                assertThat(ps.executeBatch()).hasSize(2);
                ps.setInt(1, 6);
                ps.setString(2, "f");
                ps.addBatch();
                assertThat(ps.executeLargeBatch()).hasSize(1);
                ps.setInt(1, 7);
                ps.setString(2, "g");
                ps.addBatch();
                ps.clearBatch();
                ps.clearParameters();
            }
            try (PreparedStatement ps = connection.prepareStatement("select name from t where id = ?")) {
                ps.setInt(1, 1);
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString(1)).isEqualTo("a");
                }
            }
        }

        assertThat(queries).contains("insert into t values (?, ?)", "select name from t where id = ?");
        assertThat(queries).anyMatch(q -> q.startsWith("batch insert into t"));
    }

    @Test
    @DisplayName("Все варианты prepareStatement и createStatement возвращают отслеживаемые прокси")
    void statementFactoriesReturnProxies() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            List<Statement> statements = List.of(
                    connection.createStatement(),
                    connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY),
                    connection.createStatement(
                            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT),
                    connection.prepareStatement("select 1"),
                    connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY),
                    connection.prepareStatement(
                            "select 1",
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY,
                            ResultSet.CLOSE_CURSORS_AT_COMMIT),
                    connection.prepareStatement("select 1", Statement.NO_GENERATED_KEYS),
                    connection.prepareStatement("select 1", new int[] {1}),
                    connection.prepareStatement("select 1", new String[] {"ID"}));

            assertThat(statements)
                    .allSatisfy(statement -> assertThat(statement).isInstanceOf(MonitoringStatementProxy.class));
            for (Statement statement : statements) {
                statement.close();
            }
        }
    }

    @Test
    @DisplayName("Явный commit сообщается слушателю; rollback - нет")
    void commitIsReported() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            assertThat(connection.getAutoCommit()).isFalse();
            connection.commit();
            connection.rollback();
        }

        assertThat(commits.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("DataSource: unwrap и isWrapperFor доходят до исходного источника данных")
    void dataSourceDelegation() throws SQLException {
        assertThat(dataSource.isWrapperFor(JdbcDataSource.class)).isTrue();
        assertThat(dataSource.unwrap(JdbcDataSource.class)).isInstanceOf(JdbcDataSource.class);
        assertThat(dataSource.isWrapperFor(String.class)).isFalse();
        assertThatThrownBy(() -> dataSource.unwrap(String.class)).isInstanceOf(SQLException.class);
        assertThat(dataSource.getLoginTimeout()).isZero();
        dataSource.setLoginTimeout(5);
        assertThat(dataSource.getLoginTimeout()).isEqualTo(5);
        dataSource.setLogWriter(null);
        assertThat(dataSource.getLogWriter()).isNull();
        try {
            dataSource.getParentLogger();
        } catch (java.sql.SQLFeatureNotSupportedException ignored) {
            // источник данных не ведёт java.util.logging
        }
        try (Connection withCredentials = dataSource.getConnection("", "")) {
            assertThat(withCredentials).isInstanceOf(MonitoringConnectionProxy.class);
        }
    }

    @Test
    @DisplayName("Connection делегирует все методы исходному соединению")
    void connectionDelegatesEveryMethod() {
        Connection target = mock(Connection.class);
        assertDelegatesAll(Connection.class, new MonitoringConnectionProxy(target, listener), target, Set.of());
    }

    @Test
    @DisplayName("Statement делегирует все методы исходному")
    void statementDelegatesEveryMethod() {
        Statement target = mock(Statement.class);
        assertDelegatesAll(Statement.class, new MonitoringStatementProxy(target, listener), target, Set.of());
    }

    @Test
    @DisplayName("PreparedStatement делегирует все методы исходному")
    void preparedStatementDelegatesEveryMethod() {
        PreparedStatement target = mock(PreparedStatement.class);
        assertDelegatesAll(
                PreparedStatement.class,
                new MonitoringPreparedStatementProxy(target, "select 1", listener),
                target,
                Set.of());
    }

    @Test
    @DisplayName("CompositeSqlExecutionListener: сбой одного слушателя не мешает остальным")
    void compositeListenerIsolatesFailures() {
        List<String> received = new ArrayList<>();
        SqlExecutionListener failing = new SqlExecutionListener() {
            @Override
            public void onQueryExecuted(String sql, long startedAtNanos) {
                throw new IllegalStateException("boom");
            }

            @Override
            public void onCommit() {
                throw new IllegalStateException("boom");
            }
        };
        SqlExecutionListener recording = new SqlExecutionListener() {
            @Override
            public void onQueryExecuted(String sql, long startedAtNanos) {
                received.add(sql);
            }

            @Override
            public void onCommit() {
                received.add("commit");
            }
        };
        CompositeSqlExecutionListener composite = new CompositeSqlExecutionListener(List.of(failing, recording));

        composite.onQueryExecuted("select 1", System.nanoTime());
        composite.onCommit();

        assertThat(received).containsExactly("select 1", "commit");
    }

    @Test
    @DisplayName("SlowQueryLogger пишет только запросы дольше порога; порог <= 0 выключает его")
    void slowQueryLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger(SlowQueryLogger.LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        Level previous = logger.getLevel();
        logger.setLevel(Level.WARN);
        try {
            long longAgo = System.nanoTime() - 5_000_000_000L;
            new SlowQueryLogger(1000).onQueryExecuted("slow sql", longAgo);
            new SlowQueryLogger(10_000).onQueryExecuted("fast sql", System.nanoTime());
            new SlowQueryLogger(0).onQueryExecuted("disabled sql", longAgo);

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .hasSize(1)
                    .first()
                    .asString()
                    .contains("slow sql");
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previous);
        }
    }

    /**
     * Вызывает каждый публичный метод интерфейса на прокси со «значениями по умолчанию» и проверяет, что исходному
     * объекту досталось то же обращение. Ловит пропущенное делегирование.
     */
    static void assertDelegatesAll(Class<?> iface, Object proxy, Object target, Set<String> skip) {
        List<String> notDelegated = new ArrayList<>();
        for (Method method : iface.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isDefault() || skip.contains(method.getName())) {
                continue;
            }
            Mockito.clearInvocations(target);
            try {
                method.invoke(proxy, defaults(method.getParameterTypes()));
            } catch (InvocationTargetException | IllegalAccessException e) {
                notDelegated.add(method + " threw " + e.getCause());
                continue;
            }
            boolean called = Mockito.mockingDetails(target).getInvocations().stream()
                    .anyMatch(invocation -> invocation.getMethod().getName().equals(method.getName())
                            && invocation.getMethod().getParameterCount() == method.getParameterCount());
            if (!called) {
                notDelegated.add(method.toString());
            }
        }
        assertThat(notDelegated).isEmpty();
    }

    private static Object[] defaults(Class<?>[] types) {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = defaultValue(types[i]);
        }
        return args;
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == char.class) {
            return 'a';
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == String.class) {
            return "x";
        }
        if (type == Class.class) {
            return Object.class;
        }
        if (type == Object.class) {
            return new Object();
        }
        if (type.isArray()) {
            return java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
        }
        if (type.isInterface()) {
            return mock(type);
        }
        return null;
    }
}
