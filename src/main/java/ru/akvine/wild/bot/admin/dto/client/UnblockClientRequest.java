package ru.akvine.wild.bot.admin.dto.client;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UnblockClientRequest extends BlockRequest {
}
