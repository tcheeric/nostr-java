package nostr.config;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:relays.properties")
@EnableConfigurationProperties(RelaysProperties.class)
public class RelayConfig {

  @Bean
  public Map<String, String> relays(RelaysProperties relaysProperties) {
    return relaysProperties;
  }

  /**
   * @deprecated Use {@link RelaysProperties} instead for relay configuration.
   *             This method will be removed in version 1.0.0.
   */
  @Deprecated(forRemoval = true, since = "0.6.2")
  private Map<String, String> legacyRelays() {
    var relaysBundle = ResourceBundle.getBundle("relays");
    return relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
  }
}
