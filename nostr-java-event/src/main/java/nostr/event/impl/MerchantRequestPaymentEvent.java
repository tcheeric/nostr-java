package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.PaymentRequest;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Merchant Request Payment Event", nip = 15)
@NoArgsConstructor
public class MerchantRequestPaymentEvent extends CheckoutEvent<PaymentRequest> {

  public MerchantRequestPaymentEvent(
      PublicKey sender, List<BaseTag> tags, @NonNull String content) {
    super(sender, tags, content, MessageType.PAYMENT_REQUEST);
  }

  public PaymentRequest getPaymentRequest() {
    return IEvent.MAPPER_BLACKBIRD.convertValue(getContent(), PaymentRequest.class);
  }

  protected PaymentRequest getEntity() {
    return getPaymentRequest();
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.ENCRYPTED_DIRECT_MESSAGE.getValue());
    }
  }
}
