package ru.akvine.marketspace.bot.context;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClientData {
    private String categoryId;
    private byte[] newCardPhoto;
}
