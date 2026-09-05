package ru.akvine.wild.bot.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import ru.akvine.wild.bot.enums.ClientState;

@Converter
public class ClientStatesConverter implements AttributeConverter<List<ClientState>, String> {
    private static final String DELIMITER = "->";

    @Override
    public String convertToDatabaseColumn(List<ClientState> states) {
        StringBuilder sb = new StringBuilder();
        int lastElementIndex = states.size() - 1;

        for (int i = 0; i < states.size(); ++i) {
            if (i == lastElementIndex) {
                sb.append(states.get(i));
            } else {
                sb.append(states.get(i)).append(DELIMITER);
            }
        }
        return sb.toString();
    }

    @Override
    public List<ClientState> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .map(ClientState::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
