package ru.akvine.wild.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.akvine.wild.bot.services.property.PropertyCodes;

@SpringBootApplication
public class WildBotApplication {

    public static void main(String[] args) throws ClassNotFoundException {

        // Прогрев статических блоков в POJO-классах (не из Spring)
        Class.forName(PropertyCodes.class.getName());
        SpringApplication.run(WildBotApplication.class, args);
    }
}
