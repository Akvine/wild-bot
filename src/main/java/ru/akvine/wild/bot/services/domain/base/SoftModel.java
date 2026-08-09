package ru.akvine.wild.bot.services.domain.base;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class SoftModel extends Model {
    protected LocalDateTime deletedDate;
    protected boolean deleted;
}
