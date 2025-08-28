package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.event.JsonContent;
import nostr.event.impl.CheckoutEvent;

public abstract class NIP15Content implements JsonContent {

  public abstract String getId();

  public String toString() {
    return value();
  }

  @EqualsAndHashCode(callSuper = true)
  @Data
  public abstract static class CheckoutContent extends NIP15Content {
    @JsonProperty private CheckoutEvent.MessageType messageType;
  }

  public abstract static class MerchantContent extends NIP15Content {}
}
