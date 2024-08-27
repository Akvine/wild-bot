package ru.akvine.marketspace.bot.admin.dto.advert;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ListAdvertRequest extends SecretRequest {
    @NotNull
    private List<String> statuses;
}
