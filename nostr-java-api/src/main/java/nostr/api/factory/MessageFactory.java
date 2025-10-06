package nostr.api.factory;

import lombok.NoArgsConstructor;
import nostr.event.BaseMessage;

/**
 * Legacy message factory abstraction; prefer BaseMessageFactory.
 *
 * @param <T> message type
 */
@NoArgsConstructor
public abstract class MessageFactory<T extends BaseMessage> {

  /** Build the message instance. */
  public abstract T create();
}
