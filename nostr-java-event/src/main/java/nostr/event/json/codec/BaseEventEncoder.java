package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.Encoder;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;

@Data
public class BaseEventEncoder<T extends GenericEvent> implements Encoder {

  private final T event;

  public BaseEventEncoder(T event) {
    this.event = event;
  }

  @Override
  public String encode() throws nostr.event.json.codec.EventEncodingException {
    try {
      return EventJsonMapper.getMapper().writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode event to JSON", e);
    }
  }
}
