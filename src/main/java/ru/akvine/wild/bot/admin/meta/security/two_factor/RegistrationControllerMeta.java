package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.registration.*;

/**
 * Контракт двухфакторной регистрации в админ-панели — последовательность {@code start} →
 * (опционально {@code newotp}) → {@code check} (проверка OTP) → (опционально
 * {@code passwordValidate}) → {@code finish} (установка пароля, завершение регистрации).
 */
@RequestMapping("/security/two/factor/registration")
public interface RegistrationControllerMeta {
    /**
     * Запускает регистрацию нового пользователя: отправляет одноразовый код на указанный email.
     *
     * @param request            email нового пользователя
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия регистрации
     * @return успешный ответ
     */
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody RegistrationStartRequest request, HttpServletRequest httpServletRequest);

    /**
     * Проверяет введённый пользователем одноразовый код.
     *
     * @param request            введённый одноразовый код
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия регистрации
     * @return успешный ответ
     */
    @PostMapping(value = "/check")
    Response check(@Valid @RequestBody RegistrationCheckOtpRequest request, HttpServletRequest httpServletRequest);

    /**
     * Запрашивает повторную отправку одноразового кода.
     *
     * @param request            данные текущей сессии регистрации
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия регистрации
     * @return успешный ответ
     */
    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody RegistrationNewOtpRequest request, HttpServletRequest httpServletRequest);

    /**
     * Проверяет пароль на соответствие требованиям политики паролей, не сохраняя его.
     *
     * @param request проверяемый пароль
     * @return успешный ответ
     */
    @PostMapping(value = "/password/validate")
    Response passwordValidate(@Valid @RequestBody RegistrationPasswordValidateRequest request);

    /**
     * Завершает регистрацию: устанавливает пароль и создаёт учётную запись пользователя.
     *
     * @param request            новый пароль
     * @param httpServletRequest текущий HTTP-запрос, в котором отслеживается сессия регистрации
     * @return успешный ответ
     */
    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody RegistrationFinishRequest request, HttpServletRequest httpServletRequest);
}
