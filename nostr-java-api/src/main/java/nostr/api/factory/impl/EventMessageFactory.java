package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.BaseMessageFactory;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
public class EventMessageFactory extends BaseMessageFactory<EventMessage> {

    private final GenericEvent event;
    private String subscriptionId;

    public EventMessageFactory(@NonNull GenericEvent event) {
        this.event = event;
    }

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
