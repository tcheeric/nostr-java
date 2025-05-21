package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @author eric
 */
public interface JsonContent {

  default String value() {
    try {
      return MAPPER_AFTERBURNER.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
