package ru.akvine.wild.bot.admin.dto.client;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClientDto {
    private String uuid;

    private String chatId;

    private String username;

    private String firstName;

    private String lastName;

    private int availableTestsCount;

    private boolean inWhitelist;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private LocalDateTime deletedDate;

    private boolean deleted;
}
