package nostr.event;

import lombok.Getter;
import nostr.event.json.codec.EventEncodingException;

/**
 * @author squirrel
 */
@Getter
public abstract class BaseMessage {
  private final String command;

  protected BaseMessage(String command) {
    this.command = command;
  }

  public abstract String encode() throws EventEncodingException;
}
