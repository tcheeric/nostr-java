package nostr.event;

import static nostr.base.json.EventJsonMapper.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author eric
 */
public interface JsonContent {

  default String value() {
    try {
      return mapper().writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
