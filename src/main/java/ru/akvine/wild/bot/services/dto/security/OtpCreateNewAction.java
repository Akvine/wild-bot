package ru.akvine.wild.bot.services.dto.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OtpCreateNewAction {
    private String login;
    private String newValue;
    private String sessionId;
    private boolean isCredentialsValid;

    public OtpCreateNewAction(String login, String sessionId, boolean isCredentialsValid) {
        this.login = login;
        this.sessionId = sessionId;
        this.isCredentialsValid = isCredentialsValid;
    }

    public OtpCreateNewAction(String login, String newValue, String sessionId, boolean isCredentialsValid) {
        this.login = login;
        this.newValue = newValue;
        this.sessionId = sessionId;
        this.isCredentialsValid = isCredentialsValid;
    }
}
