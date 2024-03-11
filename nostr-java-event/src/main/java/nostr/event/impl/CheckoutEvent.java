package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.IContent;

/**
 *
 * @author eric
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public abstract class CheckoutEvent extends DirectMessageEvent {

    public CheckoutEvent(PublicKey sender, PublicKey recipient, IContent content) {
        super(sender, recipient, content.toString());
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
