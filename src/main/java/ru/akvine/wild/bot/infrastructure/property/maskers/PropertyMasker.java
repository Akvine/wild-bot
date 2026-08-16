package ru.akvine.wild.bot.infrastructure.property.maskers;

import ru.akvine.wild.bot.enums.SensitiveDataType;

/**
 * Стратегия маскирования значения чувствительного property перед выводом в лог. Каждая
 * реализация отвечает за один {@link SensitiveDataType} ({@link #getType()}) и своим способом
 * прячет значение ({@link #mask(String)}) — например, оставляя видимыми только первые/последние
 * символы или заменяя значение целиком на маску.
 */
public interface PropertyMasker {
    /**
     * Маскирует значение свойства перед выводом в лог.
     *
     * @param property исходное (немаскированное) значение
     * @return маскированное значение
     */
    String mask(String property);

    /**
     * Возвращает тип чувствительных данных, за который отвечает эта маска.
     *
     * @return тип чувствительных данных
     */
    SensitiveDataType getType();
}
