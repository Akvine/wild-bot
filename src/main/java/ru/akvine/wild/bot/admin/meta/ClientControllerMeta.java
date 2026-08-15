package ru.akvine.wild.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.client.*;
import ru.akvine.wild.bot.admin.dto.common.Response;

/**
 * Контракт админ-панели для управления клиентами бота — просмотр, блокировка/разблокировка,
 * начисление тестов, управление белым списком, рассылка сообщений и QR-кодов.
 */
@RequestMapping(value = "/admin/clients")
public interface ClientControllerMeta {
    /**
     * Возвращает список всех клиентов.
     *
     * @return список клиентов
     */
    @GetMapping
    Response list();

    /**
     * Начисляет клиенту дополнительные тесты (запуски рекламных кампаний).
     *
     * @param request идентификатор клиента и количество начисляемых тестов
     * @return успешный ответ
     */
    @PostMapping(value = "/add/tests")
    Response addTests(@Valid @RequestBody AddTestsRequest request);

    /**
     * Блокирует клиента.
     *
     * @param request идентификатор клиента и параметры блокировки
     * @return успешный ответ
     */
    @PostMapping(value = "/block")
    Response block(@Valid @RequestBody BlockClientRequest request);

    /**
     * Снимает блокировку с клиента.
     *
     * @param request идентификатор клиента
     * @return успешный ответ
     */
    @PostMapping(value = "/unblock")
    Response unblock(@Valid @RequestBody UnblockClientRequest request);

    /**
     * Возвращает список заблокированных клиентов.
     *
     * @return список заблокированных клиентов
     */
    @PostMapping(value = "/block/list")
    Response listBlocked();

    /**
     * Отправляет клиенту (или группе клиентов) сообщение через бота.
     *
     * @param request получатель(и) и текст сообщения
     * @return успешный ответ
     */
    @PostMapping(value = "/send/message")
    Response sendMessage(@Valid @RequestBody SendMessageRequest request);

    /**
     * Добавляет клиента в белый список (разрешённых пользователей бота).
     *
     * @param request идентификатор клиента
     * @return успешный ответ
     */
    @PostMapping(value = "/whitelist/add")
    Response addToWhitelist(@Valid @RequestBody WhitelistRequest request);

    /**
     * Удаляет клиента из белого списка.
     *
     * @param request идентификатор клиента
     * @return успешный ответ
     */
    @PostMapping(value = "/whitelist/delete")
    Response deleteFromWhitelist(@Valid @RequestBody WhitelistRequest request);

    /**
     * Отправляет клиенту QR-код через бота.
     *
     * @param request идентификатор клиента и параметры генерации QR-кода
     * @return успешный ответ
     */
    @PostMapping(value = "/send/qr-code")
    Response sendQrCode(@Valid @RequestBody SendQrCodeRequest request);
}
