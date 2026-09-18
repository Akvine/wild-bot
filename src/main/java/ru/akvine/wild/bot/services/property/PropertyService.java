package ru.akvine.wild.bot.services.property;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Сервис для работы со свойствами приложения.
 * <p>
 * Предоставляет методы для получения, проверки наличия, изменения и типизированного
 * чтения свойств.
 */
public interface PropertyService {

    /**
     * Добавляет или заменяет значение свойства.
     * <p>
     * Если свойство с указанным ключом уже существует, его значение заменяется.
     * Иначе создаётся новая запись.
     *
     * @param propertyKey ключ свойства
     * @param value       новое значение свойства
     * @throws NullPointerException если {@code propertyKey} или {@code value} равен {@code null}
     */
    void put(String propertyKey, String value);

    /**
     * Возвращает значение свойства по ключу.
     *
     * @param propertyKey ключ свойства
     * @return значение свойства
     * @throws NullPointerException     если {@code propertyKey} равен {@code null}
     * @throws NoSuchElementException   если свойство с указанным ключом не найдено
     */
    String get(String propertyKey);

    /**
     * Проверяет наличие свойства с указанным ключом.
     *
     * @param propertyKey ключ свойства
     * @return {@code true}, если свойство существует; иначе {@code false}
     * @throws NullPointerException если {@code propertyKey} равен {@code null}
     */
    boolean contains(String propertyKey);

    /**
     * Возвращает значение свойства, преобразованное к указанному типу.
     * <p>
     * Поддерживаются типы: {@link Long}, {@link Integer}, {@link String},
     * {@link Boolean}, {@link Double}.
     *
     * @param propertyKey ключ свойства
     * @param type        класс целевого типа
     * @param <T>         целевой тип
     * @return значение свойства в указанном типе
     * @throws IllegalStateException    если свойство с указанным ключом отсутствует
     * @throws IllegalArgumentException если указанный тип не поддерживается
     * @throws NumberFormatException    если значение не удалось преобразовать к числовому типу
     */
    <T> T getAs(String propertyKey, Class<T> type);

    /**
     * Возвращает значение свойства, разделённое по разделителю, в виде списка строк.
     *
     * @param propertyKey ключ свойства
     * @param delimiter   разделитель (регулярное выражение)
     * @return список строк; если значение свойства равно {@code null}, возвращается пустой список
     * @throws IllegalStateException если свойство с указанным ключом отсутствует
     * @throws NullPointerException  если {@code delimiter} равен {@code null}
     */
    List<String> getAsList(String propertyKey, String delimiter);

    /**
     * Возвращает значение свойства, разделённое по разделителю, в виде множества строк.
     *
     * @param propertyKey ключ свойства
     * @param delimiter   разделитель (регулярное выражение)
     * @return множество строк; если значение свойства равно {@code null}, возвращается пустое множество
     * @throws IllegalStateException если свойство с указанным ключом отсутствует
     * @throws NullPointerException  если {@code delimiter} равен {@code null}
     */
    Set<String> getAsSet(String propertyKey, String delimiter);

    /**
     * Возвращает значение свойства, разделённое по разделителю, в виде списка значений указанного типа.
     *
     * @param propertyKey ключ свойства
     * @param delimiter   разделитель (регулярное выражение)
     * @param targetClazz класс элементов результирующего списка
     * @param <T>         тип элементов
     * @return список значений указанного типа
     * @throws IllegalStateException    если свойство с указанным ключом отсутствует
     * @throws IllegalArgumentException если указанный тип не поддерживается
     * @throws NumberFormatException    если значение не удалось преобразовать к числовому типу
     * @throws NullPointerException     если {@code delimiter} равен {@code null}
     */
    <T> List<T> getAsList(String propertyKey, String delimiter, Class<T> targetClazz);

    /**
     * Возвращает значение свойства, разделённое по разделителю, в виде множества значений указанного типа.
     *
     * @param propertyKey ключ свойства
     * @param delimiter   разделитель (регулярное выражение)
     * @param targetClazz класс элементов результирующего множества
     * @param <T>         тип элементов
     * @return множество значений указанного типа
     * @throws IllegalStateException    если свойство с указанным ключом отсутствует
     * @throws IllegalArgumentException если указанный тип не поддерживается
     * @throws NumberFormatException    если значение не удалось преобразовать к числовому типу
     * @throws NullPointerException     если {@code delimiter} равен {@code null}
     */
    <T> Set<T> getAsSet(String propertyKey, String delimiter, Class<T> targetClazz);

    /**
     * Возвращает неизменяемое представление всех свойств.
     *
     * @return неизменяемая карта всех свойств
     */
    Map<String, String> getAll();
}
