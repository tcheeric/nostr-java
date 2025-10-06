package nostr.api.factory;

import lombok.NoArgsConstructor;
import nostr.event.BaseMessage;

/**
 * Base message factory for building protocol messages from inputs.
 *
 * @param <T> message type
 */
@NoArgsConstructor
public abstract class BaseMessageFactory<T extends BaseMessage> {

  /** Build the message instance. */
  public abstract T create();
}
