package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthCredentialsRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthNewOtpRequest;

/**
 * Контракт двухфакторной аутентификации в админ-панели — последовательность
 * {@code start} (логин/пароль) → (опционально {@code newotp}) → {@code finish} (проверка OTP,
 * установка сессии).
 */
@RequestMapping(value = "/security/two/factor/auth")
public interface AuthControllerMeta {
    /**
     * Проверяет логин/пароль и отправляет пользователю одноразовый код для второго фактора.
     *
     * @param request            email и пароль
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия аутентификации
     * @return успешный ответ
     */
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody AuthCredentialsRequest request, HttpServletRequest httpServletRequest);

    /**
     * Запрашивает повторную отправку одноразового кода.
     *
     * @param request            данные текущей сессии аутентификации
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия аутентификации
     * @return успешный ответ
     */
    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody AuthNewOtpRequest request, HttpServletRequest httpServletRequest);

    /**
     * Проверяет введённый одноразовый код и аутентифицирует пользователя в текущей сессии.
     *
     * @param request            введённый одноразовый код
     * @param httpServletRequest текущий HTTP-запрос, в котором устанавливается сессия
     * @return успешный ответ
     */
    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody AuthFinishRequest request, HttpServletRequest httpServletRequest);

    /**
     * Завершает текущую сессию пользователя.
     *
     * @param httpServletRequest текущий HTTP-запрос
     * @return успешный ответ
     */
    @GetMapping(value = "/logout")
    Response logout(HttpServletRequest httpServletRequest);
}
