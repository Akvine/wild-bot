package ru.akvine.wild.bot.services.domain;

import java.io.Serializable;

/**
 * Неизменяемое представление типа карточки для кеша. В кеш кладётся не JPA-сущность: сущность привязана к сессии
 * Hibernate и не должна жить дольше неё, а это значение безопасно сериализуется в Redis.
 *
 * @param id идентификатор типа
 * @param type название типа
 */
public record CardTypeInfo(Long id, String type) implements Serializable {}
