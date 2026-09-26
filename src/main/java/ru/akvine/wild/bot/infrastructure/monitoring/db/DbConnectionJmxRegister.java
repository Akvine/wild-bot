package ru.akvine.wild.bot.infrastructure.monitoring.db;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import lombok.extern.slf4j.Slf4j;

/**
 * Публикует в JMX описание подключения к БД ({@link DbInfoConnection}) как MBean
 * {@code <domain>:name=connection.info}. Так по JMX видно, к какой базе подключён экземпляр приложения.
 */
@Slf4j
public class DbConnectionJmxRegister {
    private final DbInfoConnection mbean;
    private final ObjectName objectName;

    public DbConnectionJmxRegister(String domain, DbInfoConnection mbean) throws Exception {
        this.mbean = mbean;
        this.objectName = new ObjectName(domain, "name", "connection.info");
    }

    @PostConstruct
    public void register() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (!mBeanServer.isRegistered(objectName)) {
            mBeanServer.registerMBean(mbean, objectName);
            logger.info("Registered db connection info in JMX: {} -> {}", objectName, mbean);
        }
    }

    @PreDestroy
    public void unregister() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mBeanServer.isRegistered(objectName)) {
            mBeanServer.unregisterMBean(objectName);
        }
    }
}
