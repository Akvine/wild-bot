package ru.akvine.marketspace.bot.exceptions.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ErrorField {
    private String fieldName;
    private String errorMessage;
}
