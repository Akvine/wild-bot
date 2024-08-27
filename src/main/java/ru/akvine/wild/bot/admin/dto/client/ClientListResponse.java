package ru.akvine.wild.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClientListResponse extends SuccessfulResponse {
    private List<ClientDto> clients;
}
