package ru.akvine.wild.bot.config.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis")
@Getter
@Setter
public class RedisConfigProperties {
    private int port;
    private String host;
    private String password;
    private boolean clusterOn;

    public List<String> getNodes() {
        List<String> hardCodedNodes = new ArrayList<>();
        hardCodedNodes.add(host + ":" + port);
        return hardCodedNodes;
    }
}
