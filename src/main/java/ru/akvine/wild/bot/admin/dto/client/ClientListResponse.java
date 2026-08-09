package ru.akvine.wild.bot.admin.dto.client;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

@Data
@Accessors(chain = true)
public class ClientListResponse extends SuccessfulResponse {
    private List<ClientDto> clients;
}
