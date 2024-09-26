package ru.akvine.wild.bot.repositories.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.security.OtpCounterEntity;

public interface OtpCounterRepository extends JpaRepository<OtpCounterEntity, Long> {
    @Query("from OtpCounterEntity oce where oce.login = :login")
    OtpCounterEntity findByLogin(@Param("login") String login);
}
