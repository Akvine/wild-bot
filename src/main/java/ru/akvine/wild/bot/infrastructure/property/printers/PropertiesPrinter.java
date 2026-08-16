package ru.akvine.wild.bot.infrastructure.property.printers;

import java.util.Map;

/**
 * Выводит набор свойств приложения куда-либо (например, в лог) для диагностики конфигурации
 * при старте. Единственная реализация — {@link LogPropertiesPrinter}.
 */
public interface PropertiesPrinter {
    /**
     * Выводит переданные свойства.
     *
     * @param properties свойства в виде пар имя-значение
     */
    void print(Map<String, String> properties);
}
