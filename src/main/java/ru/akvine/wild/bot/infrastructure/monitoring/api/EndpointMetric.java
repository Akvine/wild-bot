package ru.akvine.wild.bot.infrastructure.monitoring.api;

import com.codahale.metrics.Timer;

/**
 * Эндпоинт приложения и его {@link Timer}. Таймер создаётся при первом обращении к эндпоинту
 */
public class EndpointMetric {
    private final String name;
    private volatile Timer timer;

    public EndpointMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return таймер или {@code null}, если к эндпоинту ещё не обращались
     */
    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
