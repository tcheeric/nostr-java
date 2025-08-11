package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.PaymentShipmentStatus;

import java.util.List;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Verify Payment Or Shipped Event", nip = 15)
@NoArgsConstructor
public class VerifyPaymentOrShippedEvent extends CheckoutEvent<PaymentShipmentStatus> {

    public VerifyPaymentOrShippedEvent(PublicKey sender, List<BaseTag> tags, @NonNull String content) {
        super(sender, tags, content, MessageType.ORDER_STATUS_UPDATE);
    }

    public PaymentShipmentStatus getPaymentShipmentStatus() {
        return IEvent.MAPPER_BLACKBIRD.convertValue(getContent(), PaymentShipmentStatus.class);
    }

    protected PaymentShipmentStatus getEntity() {
        return getPaymentShipmentStatus();
    }

    @Override
    public void validateKind() {
        if (getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.ENCRYPTED_DIRECT_MESSAGE.getValue());
        }
    }
}
