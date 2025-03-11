package nostr.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.base.IEvent;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @param <T>
 * @author eric
 */
public abstract class AbstractEventContent<T extends IEvent> implements IContent {

  @Override
  public String toString() {
    try {
      return MAPPER_AFTERBURNER.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
