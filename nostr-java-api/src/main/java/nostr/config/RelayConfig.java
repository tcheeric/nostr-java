package nostr.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Configuration
@PropertySource("classpath:relays.properties")
@EnableConfigurationProperties(RelaysProperties.class)
public class RelayConfig {

    @Bean
    public Map<String, String> relays(RelaysProperties relaysProperties) {
        return relaysProperties;
    }

    /**
     * @deprecated use {@link RelaysProperties} instead
     */
    @Deprecated
    private Map<String, String> legacyRelays() {
        var relaysBundle = ResourceBundle.getBundle("relays");
        return relaysBundle.keySet().stream()
                .collect(Collectors.toMap(key -> key, relaysBundle::getString));
    }
}
