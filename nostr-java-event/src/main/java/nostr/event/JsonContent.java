package nostr.event;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author eric
 */
public interface JsonContent {

  default String value() {
    try {
      return MAPPER_BLACKBIRD.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
