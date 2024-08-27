package ru.akvine.wild.bot.exceptions.api;

import lombok.experimental.UtilityClass;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ErrorMessageHelper {
    public List<ErrorField> extractErrorField(MethodArgumentNotValidException exception) {
        List<ErrorField> errorFields = new ArrayList<>();
        exception.getFieldErrors().forEach(errorField -> {
            errorFields.add(new ErrorField(
                    errorField.getField(),
                    errorField.getDefaultMessage()
            ));
        });
        return errorFields;
    }

    public String toErrorMessage(String fieldName, String message) {
        return toErrorMessage(new ErrorField(fieldName, message));
    }

    public String toErrorMessage(ErrorField errorField) {
        return toErrorMessage(List.of(errorField));
    }

    public String toErrorMessage(List<ErrorField> fieldsWithMessages) {
        StringBuilder sb = new StringBuilder();

        int lastElementIndex = fieldsWithMessages.size() - 1;
        for (int i = 0; i < fieldsWithMessages.size(); ++i) {
            sb
                    .append("Поле [")
                    .append(fieldsWithMessages.get(i).getFieldName())
                    .append("] ")
                    .append(fieldsWithMessages.get(i).getErrorMessage());
            if (i != lastElementIndex) {
                sb.append("; ");
            }
        }

        return sb.toString();
    }
}
