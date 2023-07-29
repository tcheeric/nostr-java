package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
public class MerchantRequestPaymentEvent extends CheckoutEvent {

    public MerchantRequestPaymentEvent(PublicKey sender, Customer customer, @NonNull Payment payment) {
        super(sender, customer.getContact().getPublicKey(), payment);
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class Payment extends AbstractContent<MerchantRequestPaymentEvent> { 

        @JsonProperty
        private final String id;
        
        @JsonProperty
        private MessageType type;
        
        @JsonProperty
        private String message;
        
        @JsonProperty("payment_options")
        private List<PaymentOptions> paymentOptions;

        public Payment() {
            this.paymentOptions = new ArrayList<>();
            this.id = UUID.randomUUID().toString();
        }

        @Data
        @NoArgsConstructor
        public static class PaymentOptions {

            public static enum Type {
                URL,
                BTC,
                LN,
                LNURL;
                
                @JsonValue
                public String getValue() {
                    return name().toLowerCase();
                }
            }

            @JsonProperty
            private Type type;
            
            @JsonProperty
            private String link;
        }
    }
}
