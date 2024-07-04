package ru.akvine.marketspace.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ListBlockClientResponse extends SuccessfulResponse {
    private long count;
    private List<BlockClientDto> list = new ArrayList<>();
}
