package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.impl.CustomerOrderEvent.Customer;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public class VerifyPaymentOrShippedEvent extends CheckoutEvent {

    public VerifyPaymentOrShippedEvent(PublicKey sender, Customer customer, PaymentShipmentStatus status) {
        super(sender, customer.getContact().getPublicKey(), status);
    }
    
    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = false)
    @ToString(callSuper = true)
    public static class PaymentShipmentStatus extends AbstractEventContent<VerifyPaymentOrShippedEvent> {

        @JsonProperty
        private final String id;
        
        @JsonProperty
        private MessageType type;
        
        @JsonProperty
        private String message;
        
        @JsonProperty
        private boolean paid;
        
        @JsonProperty
        private boolean shipped;

        public PaymentShipmentStatus() {
            this.id = UUID.randomUUID().toString();
        }
    }
}
