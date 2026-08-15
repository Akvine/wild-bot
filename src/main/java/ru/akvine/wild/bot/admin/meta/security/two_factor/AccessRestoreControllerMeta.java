package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreCheckOtpRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreStartRequest;

/**
 * Контракт восстановления доступа (сброса пароля) в админ-панели через OTP —
 * последовательность {@code start} → (опционально {@code newotp}) → {@code check} →
 * {@code finish}.
 */
@RequestMapping(value = "/security/two/factor/access/restore")
public interface AccessRestoreControllerMeta {
    /**
     * Запускает восстановление доступа: отправляет пользователю одноразовый код.
     *
     * @param request            email пользователя
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия восстановления
     * @return успешный ответ
     */
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody AccessRestoreStartRequest request, HttpServletRequest httpServletRequest);

    /**
     * Запрашивает повторную отправку одноразового кода.
     *
     * @param request            email пользователя
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия восстановления
     * @return успешный ответ
     */
    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody AccessRestoreStartRequest request, HttpServletRequest httpServletRequest);

    /**
     * Проверяет введённый пользователем одноразовый код.
     *
     * @param request            введённый одноразовый код
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия восстановления
     * @return успешный ответ
     */
    @PostMapping(value = "/check")
    Response check(@Valid @RequestBody AccessRestoreCheckOtpRequest request, HttpServletRequest httpServletRequest);

    /**
     * Завершает восстановление доступа, устанавливая новый пароль.
     *
     * @param request            новый пароль
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия восстановления
     * @return успешный ответ
     */
    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody AccessRestoreFinishRequest request, HttpServletRequest httpServletRequest);
}
