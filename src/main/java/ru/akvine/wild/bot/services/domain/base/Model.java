package ru.akvine.wild.bot.services.domain.base;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public abstract class Model {
    protected LocalDateTime createdDate;
    @Nullable
    protected LocalDateTime updatedDate;
}
