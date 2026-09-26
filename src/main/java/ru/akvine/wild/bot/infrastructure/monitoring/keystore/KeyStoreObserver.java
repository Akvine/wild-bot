package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import java.util.List;

public interface KeyStoreObserver {
    List<ExpireCertData> observe();
}
