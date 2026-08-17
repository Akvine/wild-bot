package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

@Component
@Slf4j
@RequiredArgsConstructor
public class BenchmarkFilter extends MessageFilter {
    @Override
    public Response handle(Payload payload) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        Response response = nextMessageFilter.handle(payload);

        timeMeter.stop();
        logger.info("Request time execution seconds: {}", timeMeter.getTotalTimeSeconds());
        return response;
    }
}
