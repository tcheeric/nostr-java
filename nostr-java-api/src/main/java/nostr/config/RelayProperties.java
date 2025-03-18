package nostr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Configuration
@PropertySource("classpath:relays.properties")
public class RelayProperties {

    @Bean
    public Map<String, String> relays() {
        ResourceBundle relaysBundle = ResourceBundle.getBundle("relays");
        return relaysBundle.keySet().stream()
            .collect(Collectors.toMap(key -> key, relaysBundle::getString));
    }
}
