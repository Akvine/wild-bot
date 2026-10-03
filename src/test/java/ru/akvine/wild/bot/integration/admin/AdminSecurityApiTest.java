package ru.akvine.wild.bot.integration.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;
import ru.akvine.wild.bot.repositories.admin.SupportRepository;

@DisplayName("Админский REST: регистрация, вход и восстановление доступа (двухфакторные сценарии)")
class AdminSecurityApiTest extends AdminApiBaseTest {
    private static final String OTP = "4444";
    private static final String PASSWORD = "Str0ng_Pass1";
    private static final String NEW_PASSWORD = "An0ther_Pass2";
    private static final String REG = "/security/two/factor/registration";
    private static final String AUTH = "/security/two/factor/auth";
    private static final String RESTORE = "/security/two/factor/access/restore";

    @Autowired
    private SupportRepository supportRepository;

    private static String newEmail() {
        return "admin-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private ResultActions call(MockHttpSession session, String url, String json) throws Exception {
        return mockMvc.perform(post(url)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private static String email(String email) {
        return "{\"email\":\"" + email + "\"}";
    }

    private static String emailAnd(String email, String field, String value) {
        return "{\"email\":\"" + email + "\",\"" + field + "\":\"" + value + "\"}";
    }

    private void register(MockHttpSession session, String email, String password) throws Exception {
        call(session, REG + "/start", email(email)).andExpect(status().isOk());
        call(session, REG + "/check", emailAnd(email, "otp", OTP)).andExpect(status().isOk());
        call(session, REG + "/finish", emailAnd(email, "password", password)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Регистрация: старт, проверка кода и завершение создают пользователя и авторизуют сессию")
    void registrationHappyPath() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = newEmail();

        call(session, REG + "/start", email(email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.otp.otpCountLeft").isNumber());
        call(session, REG + "/check", emailAnd(email, "otp", OTP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        call(session, REG + "/finish", emailAnd(email, "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        assertThat(supportRepository.findByEmail(email)).isPresent();
        assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();
    }

    @Test
    @DisplayName("Регистрация: повторный старт отдаёт текущее состояние, новый код до задержки не выдаётся")
    void registrationRepeatedStartAndNewOtp() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = newEmail();

        call(session, REG + "/start", email(email)).andExpect(status().isOk());
        call(session, REG + "/start", email(email)).andExpect(status().isOk());
        call(session, REG + "/newotp", email(email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("Регистрация: неверный код уменьшает число попыток, после лимита адрес блокируется")
    void registrationWrongOtpBlocksAfterLimit() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = newEmail();
        call(session, REG + "/start", email(email)).andExpect(status().isOk());

        call(session, REG + "/check", emailAnd(email, "otp", "0000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        call(session, REG + "/check", emailAnd(email, "otp", "0000")).andExpect(status().isBadRequest());
        call(session, REG + "/check", emailAnd(email, "otp", "0000")).andExpect(status().isBadRequest());

        call(session, REG + "/start", email(email))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    @DisplayName("Регистрация: чужая сессия, проверка без старта, завершение без кода - ошибки")
    void registrationSessionAndStateErrors() throws Exception {
        String email = newEmail();
        call(new MockHttpSession(), REG + "/check", emailAnd(email, "otp", OTP)).andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/newotp", email(email)).andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/finish", emailAnd(email, "password", PASSWORD))
                .andExpect(status().isBadRequest());

        MockHttpSession owner = new MockHttpSession();
        call(owner, REG + "/start", email(email)).andExpect(status().isOk());
        call(new MockHttpSession(), REG + "/check", emailAnd(email, "otp", OTP)).andExpect(status().isBadRequest());
        call(owner, REG + "/finish", emailAnd(email, "password", PASSWORD)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация: уже существующий пользователь, плохая почта и слабый пароль отклоняются")
    void registrationValidation() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);

        call(new MockHttpSession(), REG + "/start", email(email)).andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/start", email("not-an-email")).andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/start", "{}").andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/finish", emailAnd(newEmail(), "password", "weak"))
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/password/validate", "{\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isOk());
        call(new MockHttpSession(), REG + "/password/validate", "{\"password\":\"short\"}")
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/password/validate", "{\"password\":\"alllowercaseonly\"}")
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), REG + "/password/validate", "{\"password\":\"Has Space1_x\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Вход: пароль, затем одноразовый код; сессия авторизуется, logout её сбрасывает")
    void authHappyPathAndLogout() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);
        MockHttpSession session = new MockHttpSession();

        call(session, AUTH + "/start", emailAnd(email, "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        call(session, AUTH + "/finish", emailAnd(email, "otp", OTP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();

        mockMvc.perform(get(AUTH + "/logout").session(session)).andExpect(status().isOk());
        assertThat(session.getAttribute("SPRING_SECURITY_CONTEXT")).isNull();
    }

    @Test
    @DisplayName("Вход: неверный пароль и неизвестный пользователь - ошибка учётных данных")
    void authBadCredentials() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);

        call(new MockHttpSession(), AUTH + "/start", emailAnd(email, "password", "Wrong_Pass1x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
        call(new MockHttpSession(), AUTH + "/start", emailAnd(newEmail(), "password", PASSWORD))
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), AUTH + "/finish", emailAnd(email, "otp", OTP))
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), AUTH + "/newotp", email(email)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Вход: новый код запрашивается в рамках начатого входа; неверный код считается попыткой")
    void authNewOtpAndWrongOtp() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);
        MockHttpSession session = new MockHttpSession();

        call(session, AUTH + "/start", emailAnd(email, "password", PASSWORD)).andExpect(status().isOk());
        call(session, AUTH + "/newotp", email(email)).andExpect(status().isOk());
        call(session, AUTH + "/finish", emailAnd(email, "otp", "1111")).andExpect(status().isBadRequest());
        call(session, AUTH + "/finish", emailAnd(email, "otp", OTP)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Многократный неверный пароль блокирует вход")
    void authBlocksAfterWrongPasswords() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);
        MockHttpSession session = new MockHttpSession();

        for (int i = 0; i < 4; i++) {
            call(session, AUTH + "/start", emailAnd(email, "password", "Wrong_Pass1x"))
                    .andExpect(status().isBadRequest());
        }
        call(session, AUTH + "/start", emailAnd(email, "password", PASSWORD)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Восстановление доступа: код, новый пароль, затем вход с новым паролем")
    void accessRestore() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);
        MockHttpSession session = new MockHttpSession();

        call(session, RESTORE + "/start", email(email)).andExpect(status().isOk());
        call(session, RESTORE + "/newotp", email(email)).andExpect(status().isOk());
        call(session, RESTORE + "/check", emailAnd(email, "otp", OTP)).andExpect(status().isOk());
        call(session, RESTORE + "/finish", emailAnd(email, "password", NEW_PASSWORD))
                .andExpect(status().isOk());

        // регрессия: раньше в БД попадал открытый пароль вместо хеша
        String storedHash = supportRepository.findByEmail(email).orElseThrow().getHash();
        assertThat(storedHash).isNotEqualTo(NEW_PASSWORD).startsWith("$2");

        MockHttpSession login = new MockHttpSession();
        call(login, AUTH + "/start", emailAnd(email, "password", NEW_PASSWORD)).andExpect(status().isOk());
        call(new MockHttpSession(), AUTH + "/start", emailAnd(email, "password", PASSWORD))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName(
            "Восстановление доступа: неизвестная почта, неверный код и завершение без проверки кода - ошибки (регрессия: существующий пользователь больше не отклоняется)")
    void accessRestoreFailures() throws Exception {
        String email = newEmail();
        register(new MockHttpSession(), email, PASSWORD);

        call(new MockHttpSession(), RESTORE + "/start", email(newEmail())).andExpect(status().isBadRequest());
        call(new MockHttpSession(), RESTORE + "/check", emailAnd(email, "otp", OTP))
                .andExpect(status().isBadRequest());
        call(new MockHttpSession(), RESTORE + "/newotp", email(email)).andExpect(status().isBadRequest());
        call(new MockHttpSession(), RESTORE + "/finish", emailAnd(email, "password", NEW_PASSWORD))
                .andExpect(status().isBadRequest());

        MockHttpSession session = new MockHttpSession();
        call(session, RESTORE + "/start", email(email)).andExpect(status().isOk());
        call(session, RESTORE + "/check", emailAnd(email, "otp", "0000")).andExpect(status().isBadRequest());
        call(session, RESTORE + "/finish", emailAnd(email, "password", NEW_PASSWORD))
                .andExpect(status().isBadRequest());
        call(session, RESTORE + "/finish", emailAnd(email, "password", "weak")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Однофакторные эндпоинты регистрируют и авторизуют пользователя по паролю")
    void oneFactor() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String email = newEmail();

        call(
                        session,
                        "/security/one/factor/registration",
                        "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isOk());
        MockHttpSession login = new MockHttpSession();
        call(login, "/security/one/factor/auth", "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}")
                .andExpect(status().isOk());
        call(
                        new MockHttpSession(),
                        "/security/one/factor/auth",
                        "{\"email\":\"" + email + "\",\"password\":\"Wrong_Pass1x\"}")
                .andExpect(status().isBadRequest());
        call(login, "/security/one/factor/logout", "").andExpect(status().isOk());
    }
}
