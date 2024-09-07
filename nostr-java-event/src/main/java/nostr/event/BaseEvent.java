package nostr.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import nostr.base.IEvent;
import nostr.event.impl.GenericEvent;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(
        value = GenericEvent.class)
})
@SuperBuilder

@NoArgsConstructor
public abstract class BaseEvent implements IEvent {
}
