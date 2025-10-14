package nostr.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource("classpath:relays.properties")
@EnableConfigurationProperties(RelaysProperties.class)
public class RelayConfig {

  @Bean
  public Map<String, String> relays(RelaysProperties relaysProperties) {
    return relaysProperties;
  }

  // Legacy property loader removed in 1.0.0. Use RelaysProperties bean instead.
}
