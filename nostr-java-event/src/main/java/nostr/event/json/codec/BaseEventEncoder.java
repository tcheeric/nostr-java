package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import nostr.base.Encoder;
import nostr.event.BaseEvent;

@Data
public class BaseEventEncoder<T extends BaseEvent> implements Encoder {

  private final T event;

  public BaseEventEncoder(T event) {
    this.event = event;
  }

  @Override
  //    TODO: refactor all methods calling this to properly handle invalid json exception
  public String encode() throws nostr.event.json.codec.EventEncodingException {
    try {
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode event to JSON", e);
    }
  }
}
