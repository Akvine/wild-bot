package ru.akvine.wild.bot.integration.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;

@DisplayName("Админский REST: карточки и подписки")
class AdminCardAndSubscriptionApiTest extends AdminApiBaseTest {

    @Test
    @DisplayName("Список карточек с фильтрами по названию, id и категории")
    void listCards() throws Exception {
        ClientEntity client = newClientWithToken();
        CardEntity card = newCard(client);

        getJson(
                        "/admin/cards",
                        "{\"filter\":{\"externalId\":" + card.getExternalId()
                                + "},\"nextPage\":{\"page\":0,\"count\":10}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        getJson(
                        "/admin/cards",
                        "{\"filter\":{\"externalTitle\":\"Card\",\"categoryTitle\":\"Category\",\"categoryId\":77},"
                                + "\"nextPage\":{\"page\":0,\"count\":10}}")
                .andExpect(status().isOk());
        getJson("/admin/cards", "{\"nextPage\":{\"page\":0,\"count\":3}}").andExpect(status().isOk());
        getJson("/admin/cards", "{}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Подписка: добавление создаёт срок действия, чтение возвращает её, удаление снимает")
    void subscriptionLifecycle() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);
        String request = "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}";

        postJson("/admin/subscriptions/add", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        assertThat(subscriptionRepository.findByChatId(chatId)).isPresent();
        assertThat(subscriptionRepository.findByChatId(chatId).orElseThrow().getExpiresAt())
                .isAfter(LocalDateTime.now());

        postJson("/admin/subscriptions/get", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        postJson("/admin/subscriptions/add", request).andExpect(status().isOk());

        postJson("/admin/subscriptions/delete", request).andExpect(status().isOk());
        assertThat(subscriptionRepository.findByChatId(chatId)).isEmpty();
    }

    @Test
    @DisplayName("Подписка по username клиента")
    void subscriptionByUsername() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);
        String request = "{\"username\":\"user" + chatId + "\"}";

        postJson("/admin/subscriptions/add", request).andExpect(status().isOk());
        postJson("/admin/subscriptions/get", request).andExpect(status().isOk());
        postJson("/admin/subscriptions/delete", request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Подписка: нет подписки, неизвестный клиент, пустой запрос и неверный тип бота - ошибки")
    void subscriptionFailures() throws Exception {
        String chatId = uniqueChatId();
        newClient(chatId, true);
        String request = "{\"chatId\":\"" + chatId + "\",\"botType\":\"telegram\"}";

        postJson("/admin/subscriptions/get", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        postJson("/admin/subscriptions/delete", request).andExpect(status().isBadRequest());
        postJson("/admin/subscriptions/add", "{\"chatId\":\"" + uniqueChatId() + "\",\"botType\":\"telegram\"}")
                .andExpect(status().isBadRequest());
        postJson("/admin/subscriptions/add", "{}").andExpect(status().isBadRequest());
        postJson("/admin/subscriptions/add", "{\"chatId\":\"" + chatId + "\",\"botType\":\"nope\"}")
                .andExpect(status().isBadRequest());
    }
}
