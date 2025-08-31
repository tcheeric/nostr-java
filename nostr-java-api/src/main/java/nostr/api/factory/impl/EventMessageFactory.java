package nostr.api.factory.impl;

import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.BaseMessageFactory;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;

@Data
@EqualsAndHashCode(callSuper = false)
public class EventMessageFactory extends BaseMessageFactory<EventMessage> {

  private final GenericEvent event;
  private String subscriptionId;

  /**
   * Initialize a factory for an EVENT message without a subscription id.
   */
  public EventMessageFactory(@NonNull GenericEvent event) {
    this.event = event;
  }

  /**
   * Initialize a factory for an EVENT message bound to a subscription id.
   */
  public EventMessageFactory(@NonNull GenericEvent event, @NonNull String subscriptionId) {
    this(event);
    this.subscriptionId = subscriptionId;
  }

  @Override
  public EventMessage create() {
    return Optional.ofNullable(subscriptionId)
        .map(subscriptionId -> new EventMessage(event, subscriptionId))
        .orElse(new EventMessage(event));
  }
}
