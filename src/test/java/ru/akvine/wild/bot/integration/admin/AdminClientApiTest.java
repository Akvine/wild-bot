package ru.akvine.wild.bot.integration.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;

@DisplayName("Админский REST: клиенты")
class AdminClientApiTest extends AdminApiBaseTest {

    @Test
    @DisplayName("Список клиентов с фильтром по chatId и постраничностью")
    void listClientsByFilter() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);

        getJson(
                        "/admin/clients",
                        "{\"filter\":{\"chatIds\":[\"" + chatId + "\"],\"botType\":\"telegram\"},"
                                + "\"nextPage\":{\"page\":0,\"count\":10}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.clients[0].chatId").value(chatId))
                .andExpect(jsonPath("$.clients[0].inWhitelist").value(true));
    }

    @Test
    @DisplayName("Список клиентов: фильтры по whitelist, токену и признаку удаления")
    void listClientsWithBooleanFilters() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, false);

        getJson(
                        "/admin/clients",
                        "{\"filter\":{\"chatIds\":[\"" + chatId + "\"],\"inWhitelist\":false,"
                                + "\"tokenIsNull\":true,\"deleted\":false},\"nextPage\":{\"page\":0,\"count\":5}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
        getJson(
                        "/admin/clients",
                        "{\"filter\":{\"chatIds\":[\"" + chatId + "\"],\"inWhitelist\":true},"
                                + "\"nextPage\":{\"page\":0,\"count\":5}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("Список клиентов без nextPage - ошибка валидации поля")
    void listClientsRequiresNextPage() throws Exception {
        getJson("/admin/clients", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("Список клиентов: слишком большая страница отклоняется")
    void listClientsRejectsHugePage() throws Exception {
        getJson("/admin/clients", "{\"nextPage\":{\"page\":0,\"count\":100000}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("Начисление тестов клиенту увеличивает счётчик")
    void addTests() throws Exception {
        String chatId = uniqueChatId();
        ClientEntity client = newClient(chatId, true);
        int before = client.getAvailableTestsCount();

        postJson("/admin/clients/add/tests", "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\",\"count\":3}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.totalAvailableTestsCount").value(before + 3))
                .andExpect(jsonPath("$.chatId").value(chatId));

        assertThat(clientRepository
                        .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                        .orElseThrow()
                        .getAvailableTestsCount())
                .isEqualTo(before + 3);
    }

    @Test
    @DisplayName("Начисление тестов: неверное количество, пустой chatId и неизвестный клиент - ошибки")
    void addTestsValidation() throws Exception {
        postJson("/admin/clients/add/tests", "{\"chatId\":\"1\",\"botType\":\"telegram\",\"count\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        postJson("/admin/clients/add/tests", "{\"botType\":\"telegram\",\"count\":1}")
                .andExpect(status().isBadRequest());
        postJson(
                        "/admin/clients/add/tests",
                        "{\"chatId\":\"" + uniqueChatId() + "\",\"botType\":\"telegram\",\"count\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        postJson("/admin/clients/add/tests", "{\"chatId\":\"1\",\"botType\":\"unknown\",\"count\":1}")
                .andExpect(status().isBadRequest());
        postJson("/admin/clients/add/tests", "not json").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Блокировка клиента по chatId, список заблокированных и разблокировка")
    void blockListAndUnblock() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);

        postJson("/admin/clients/block", "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(chatId));
        postJson("/admin/clients/block/list", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        postJson("/admin/clients/unblock", "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Блокировка и разблокировка по uuid клиента")
    void blockByUuid() throws Exception {
        ClientEntity client = newClient(uniqueChatId(), true);

        postJson("/admin/clients/block", "{\"uuid\":\"" + client.getUuid() + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk());
        postJson("/admin/clients/unblock", "{\"uuid\":\"" + client.getUuid() + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Блокировка: нужен ровно один идентификатор клиента")
    void blockRequiresExactlyOneIdentifier() throws Exception {
        postJson("/admin/clients/block", "{\"botType\":\"telegram\"}").andExpect(status().isBadRequest());
        postJson("/admin/clients/block", "{\"uuid\":\"u\",\"chatId\":\"1\",\"botType\":\"telegram\"}")
                .andExpect(status().isBadRequest());
        postJson("/admin/clients/block", "{\"uuid\":\"unknown-uuid\",\"botType\":\"telegram\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("Добавление в whitelist и удаление из него")
    void whitelist() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, false);

        postJson("/admin/clients/whitelist/add", "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk());
        assertThat(clientRepository
                        .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                        .orElseThrow()
                        .isInWhitelist())
                .isTrue();

        postJson("/admin/clients/whitelist/delete", "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk());
        assertThat(clientRepository
                        .findByChatIdAndBotType(chatId, BotType.TELEGRAM)
                        .orElseThrow()
                        .isInWhitelist())
                .isFalse();

        postJson("/admin/clients/whitelist/add", "{\"chatId\":\"" + chatId + "\",\"botType\":\"wrong\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Рассылка сообщения выбранным клиентам уходит через Telegram-сервис")
    void sendMessageToSelectedChats() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);

        postJson(
                        "/admin/clients/send/message",
                        "{\"message\":\"Привет\",\"chatIds\":[\"" + chatId + "\"],\"botType\":\"telegram\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(telegramIntegrationService).sendMessage(Set.of(chatId), "Привет");
    }

    @Test
    @DisplayName("Рассылка: неизвестные чаты - ошибка, сбой доставки - ошибка с перечнем чатов")
    void sendMessageFailures() throws Exception {
        postJson(
                        "/admin/clients/send/message",
                        "{\"message\":\"hi\",\"chatIds\":[\"" + uniqueChatId() + "\"],\"botType\":\"telegram\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));

        String chatId = uniqueChatId();
        newClient(chatId, true);
        doThrow(new IllegalStateException("blocked by user"))
                .when(telegramIntegrationService)
                .sendMessage(anySet(), anyString());
        postJson(
                        "/admin/clients/send/message",
                        "{\"message\":\"hi\",\"chatIds\":[\"" + chatId + "\"],\"botType\":\"telegram\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));

        postJson("/admin/clients/send/message", "{\"botType\":\"telegram\"}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Рассылка всем клиентам бота идёт пачками")
    void sendMessageToAllClients() throws Exception {
        newClient(uniqueChatId(), true);

        postJson("/admin/clients/send/message", "{\"message\":\"всем\",\"botType\":\"telegram\"}")
                .andExpect(status().isOk());

        verify(telegramIntegrationService, org.mockito.Mockito.atLeastOnce()).sendMessage(anySet(), eq("всем"));
    }

    @Test
    @DisplayName("Несуществующий URL даёт понятную ошибку")
    void unknownUrl() throws Exception {
        getJson("/admin/no-such-endpoint", "").andExpect(status().isBadRequest());
    }
}
