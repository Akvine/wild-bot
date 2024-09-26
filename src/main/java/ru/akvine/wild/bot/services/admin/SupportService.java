package ru.akvine.wild.bot.services.admin;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.commons.util.UUIDGenerator;
import ru.akvine.wild.bot.entities.admin.SupportUserEntity;
import ru.akvine.wild.bot.exceptions.admin.SupportUserAlreadyExistsException;
import ru.akvine.wild.bot.exceptions.admin.SupportUserNotFoundException;
import ru.akvine.wild.bot.repositories.admin.SupportRepository;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.support.SupportCreate;
import ru.akvine.wild.bot.services.security.PasswordService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportService {
    private final SupportRepository supportRepository;
    private final PasswordService passwordService;

    public SupportUserModel create(SupportCreate supportCreate) {
        Preconditions.checkNotNull(supportCreate, "supportCreate is null");
        logger.debug("Create support user by = {}", supportCreate);

        String passwordHash = passwordService.encodePassword(supportCreate.getPassword());
        SupportUserEntity supportUserToSave = new SupportUserEntity()
                .setUuid(UUIDGenerator.uuidWithNoDashes())
                .setEmail(supportCreate.getEmail())
                .setHash(passwordHash);

        SupportUserModel supportUser = new SupportUserModel(supportRepository.save(supportUserToSave));
        logger.debug("Successful save support user = {}", supportUser);
        return supportUser;
    }

    public SupportUserModel create(String email, String password) {
        Preconditions.checkNotNull(email, "email is null");
        Preconditions.checkNotNull(password, "password is null");

        try {
            verifyExistsByEmail(email);
            throw new SupportUserAlreadyExistsException("Support user with email = [" + email + "] already exists!");
        } catch (SupportUserNotFoundException exception) {
            String hash = passwordService.encodePassword(password);

            SupportUserEntity supportUser = new SupportUserEntity()
                    .setEmail(email)
                    .setHash(hash)
                    .setUuid(UUIDGenerator.uuidWithNoDashes());
            return new SupportUserModel(supportRepository.save(supportUser));
        }
    }

    public SupportUserModel updatePassword(String login, String newHash) {
        logger.info("Update password for support user  with email = {}", login);

        SupportUserEntity supportUserToUpdate = verifyExistsByEmail(login);
        supportUserToUpdate.setHash(newHash);
        supportUserToUpdate.setUpdatedDate(LocalDateTime.now());

        logger.info("Successful update password for support user with email = {}", login);
        return new SupportUserModel(supportRepository.save(supportUserToUpdate));
    }

    public SupportUserModel getByEmail(String email) {
        return new SupportUserModel(verifyExistsByEmail(email));
    }

    public SupportUserEntity verifyExistsByEmail(String email) {
        Preconditions.checkNotNull(email, "email is null");
        return supportRepository
                .findByEmail(email)
                .orElseThrow(() -> new SupportUserNotFoundException("Support user with email = [" + email + "] not found!"));
    }

    public boolean isExistsByEmail(String email) {
        try {
            verifyExistsByEmail(email);
            return true;
        } catch (SupportUserNotFoundException exception) {
            return false;
        }
    }
}
