package ru.akvine.wild.bot.services.domain.base;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public abstract class Model {
    protected LocalDateTime createdDate;

    @Nullable
    protected LocalDateTime updatedDate;
}
