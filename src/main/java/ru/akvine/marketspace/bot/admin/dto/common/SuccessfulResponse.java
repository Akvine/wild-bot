package ru.akvine.marketspace.bot.admin.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;
/**
 * Successful response status
 */
@Data
@Accessors(chain = true)
public class SuccessfulResponse implements Response {

    /**
     * Request unique identificator <br/>
     *
     * <b>Example</b>: gbhjnkme-rdcfgv-hbjnkm-7689ui-okp3ew
     */
    @NotBlank
    private final String requestId = UUIDGenerator.uuid();

    /**
     * Result response status
     */
    @NotNull
    private final ResponseStatus status = ResponseStatus.SUCCESS;
}