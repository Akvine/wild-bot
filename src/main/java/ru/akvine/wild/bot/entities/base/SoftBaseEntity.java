package ru.akvine.wild.bot.entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
public abstract class SoftBaseEntity extends BaseEntity {
    @Setter(AccessLevel.NONE)
    @Column(name = "DELETED_DATE")
    private LocalDateTime deletedDate;

    @Setter(AccessLevel.NONE)
    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted;

    public void markDeleted() {
        markDeleted(LocalDateTime.now());
    }

    public void markDeleted(LocalDateTime deletedAt) {
        this.deleted = true;
        this.deletedDate = deletedAt;
    }

    public void restore() {
        this.deleted = false;
        this.deletedDate = null;
    }
}
