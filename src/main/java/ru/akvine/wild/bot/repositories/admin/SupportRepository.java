package ru.akvine.wild.bot.repositories.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.admin.SupportUserEntity;

import java.util.Optional;

public interface SupportRepository extends JpaRepository<SupportUserEntity, Long> {
    @Query("from SupportUserEntity sue where sue.email = :email")
    Optional<SupportUserEntity> findByEmail(@Param("email") String email);
}
