package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseLog extends HttpMessageLog {
    private int status;
}
