package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nostr.event.JsonContent;
import nostr.event.impl.CheckoutEvent;

public abstract class NIP15Content implements JsonContent {

    public abstract String getId();

    @Override
    public String toString() {
        return value();
    }

    @Getter
    @Setter
    public abstract static class CheckoutContent extends NIP15Content {
        @JsonProperty
        private CheckoutEvent.MessageType messageType;
    }

    public abstract static class MerchantContent extends NIP15Content {
    }
}
