package ru.akvine.marketspace.bot.entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

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
