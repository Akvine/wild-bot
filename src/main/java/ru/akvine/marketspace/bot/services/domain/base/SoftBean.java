package ru.akvine.marketspace.bot.services.domain.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public abstract class SoftBean extends Bean {
    protected ZonedDateTime deletedDate;
    protected boolean deleted;
}