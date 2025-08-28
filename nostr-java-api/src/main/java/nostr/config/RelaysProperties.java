package nostr.config;

import java.io.Serial;
import java.util.HashMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "relays")
public class RelaysProperties extends HashMap<String, String> {
  @Serial private static final long serialVersionUID = 1L;
}
