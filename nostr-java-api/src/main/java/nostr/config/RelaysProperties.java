package nostr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;

@ConfigurationProperties(prefix = "relays")
public class RelaysProperties extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;
}
