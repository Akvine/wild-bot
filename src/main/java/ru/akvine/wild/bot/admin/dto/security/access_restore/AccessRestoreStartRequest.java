package ru.akvine.wild.bot.admin.dto.security.access_restore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.akvine.wild.bot.admin.dto.security.EmailRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccessRestoreStartRequest extends EmailRequest {
}
