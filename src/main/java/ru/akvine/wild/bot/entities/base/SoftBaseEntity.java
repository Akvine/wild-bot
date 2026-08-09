package ru.akvine.wild.bot.entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
public abstract class SoftBaseEntity extends BaseEntity {
    @Column(name = "DELETED_DATE")
    private LocalDateTime deletedDate;

    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted;
}
