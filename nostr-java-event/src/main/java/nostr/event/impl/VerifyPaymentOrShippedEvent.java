package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractContent;
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
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class PaymentShipmentStatus extends AbstractContent<VerifyPaymentOrShippedEvent> {

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
