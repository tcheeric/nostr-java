package nostr.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@TestConfiguration
@TestPropertySource("classpath:relays.properties")
public class TestConfig {

    @Bean
    public Map<String, String> relays() {
        ResourceBundle relaysBundle = ResourceBundle.getBundle("relays");
        return relaysBundle.keySet().stream()
                .collect(Collectors.toMap(key -> key, relaysBundle::getString));
    }
}
