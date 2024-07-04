package ru.akvine.marketspace.bot.services.domain.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public abstract class SoftBean extends Bean {
    protected LocalDateTime deletedDate;
    protected boolean deleted;
}