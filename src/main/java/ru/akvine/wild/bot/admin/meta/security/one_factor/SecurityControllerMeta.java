package ru.akvine.wild.bot.admin.meta.security.one_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthRequest;

/**
 * Контракт однофакторной (логин/пароль, без OTP) аутентификации админ-панели —
 * используется, когда двухфакторная аутентификация отключена конфигурацией.
 */
@RequestMapping(value = "/security/one/factor")
public interface SecurityControllerMeta {
    /**
     * Регистрирует нового пользователя и сразу аутентифицирует его в текущей сессии.
     *
     * @param request            email и пароль
     * @param httpServletRequest текущий HTTP-запрос, в котором устанавливается сессия
     * @return успешный ответ
     */
    @PostMapping(value = "/registration")
    Response registration(@RequestBody @Valid AuthRequest request, HttpServletRequest httpServletRequest);

    /**
     * Проверяет учётные данные пользователя и аутентифицирует его в текущей сессии.
     *
     * @param request            email и пароль
     * @param httpServletRequest текущий HTTP-запрос, в котором устанавливается сессия
     * @return успешный ответ
     */
    @PostMapping(value = "/auth")
    Response auth(@RequestBody @Valid AuthRequest request, HttpServletRequest httpServletRequest);

    /**
     * Завершает текущую сессию пользователя.
     *
     * @param request текущий HTTP-запрос
     * @return успешный ответ
     */
    @PostMapping(value = "/logout")
    Response logout(HttpServletRequest request);
}
