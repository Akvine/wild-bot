package ru.akvine.wild.bot.services.outbox;

import ru.akvine.wild.bot.enums.BotType;

/**
 * Текстовое сообщение клиенту бота
 *
 * @param chatId чат клиента
 * @param botType бот, через которого отправляется сообщение
 * @param text текст
 */
public record BotMessagePayload(String chatId, BotType botType, String text) {}
