package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;

import static nostr.base.json.EventJsonMapper.mapper;

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
