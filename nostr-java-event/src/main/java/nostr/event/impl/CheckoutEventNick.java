package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import nostr.base.annotation.Event;

/**
 * @author eric
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public class CheckoutEventNick extends EventDecorator {
  private final GenericEventNick genericEvent;

  public CheckoutEventNick(GenericEventNick genericEvent) {
    super(genericEvent);
    this.genericEvent = genericEvent;
  }

  public enum MessageType {
    NEW_ORDER(0, "Customer"),
    PAYMENT_REQUEST(1, "Merchant"),
    ORDER_STATUS_UPDATE(2, "Merchant");

    private final int value;
    @Getter
    private final String sentBy;

    MessageType(int value, String sentBy) {
      this.value = value;
      this.sentBy = sentBy;
    }

    @JsonValue
    public int getValue() {
      return value;
    }
  }
}
