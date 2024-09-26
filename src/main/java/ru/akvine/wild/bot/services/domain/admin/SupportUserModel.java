package ru.akvine.wild.bot.services.domain.admin;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.admin.SupportUserEntity;
import ru.akvine.wild.bot.services.domain.base.Model;

@Data
@Accessors(chain = true)
public class SupportUserModel extends Model {
    private Long id;
    private String uuid;
    @ToString.Exclude
    private String email;
    @ToString.Exclude
    private String hash;

    public SupportUserModel(SupportUserEntity supportUser) {
        this.id = supportUser.getId();
        this.uuid = supportUser.getUuid();
        this.email = supportUser.getEmail();
        this.hash = supportUser.getHash();

        this.createdDate = supportUser.getCreatedDate();
        this.updatedDate = supportUser.getUpdatedDate();
    }
}
