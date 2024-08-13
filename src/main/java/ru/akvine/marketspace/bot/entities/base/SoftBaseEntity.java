package ru.akvine.marketspace.bot.entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
public abstract class SoftBaseEntity extends BaseEntity {
    @Column(name = "DELETED_DATE")
    private ZonedDateTime deletedDate;

    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted;
}
