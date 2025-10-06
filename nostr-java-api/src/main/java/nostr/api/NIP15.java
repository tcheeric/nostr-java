package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.event.entities.CustomerOrder;
import nostr.event.entities.PaymentRequest;
import nostr.event.entities.Product;
import nostr.event.entities.Stall;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * NIP-15 helpers (Endorsements/Marketplace). Build stall/product metadata and encrypted order flows.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/15.md">NIP-15</a>
 */
public class NIP15 extends EventNostr {

  public NIP15(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a merchant request payment event (encrypted DM per NIP-04/NIP-15 flow).
   *
   * @param paymentRequest the payment request payload (bolt11/details)
   * @param customerOrder the referenced customer order containing buyer contact
   * @return this instance for chaining
   */
  public NIP15 createMerchantRequestPaymentEvent(
      @NonNull PaymentRequest paymentRequest, @NonNull CustomerOrder customerOrder) {
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.ENCRYPTED_DIRECT_MESSAGE.getValue(), paymentRequest.value())
            .create();
    genericEvent.addTag(NIP01.createPubKeyTag(customerOrder.getContact().getPublicKey()));
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a customer order event (encrypted DM per NIP-04/NIP-15 flow).
   *
   * @param customerOrder the order details including buyer contact
   * @return this instance for chaining
   */
  public NIP15 createCustomerOrderEvent(@NonNull CustomerOrder customerOrder) {
    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.ENCRYPTED_DIRECT_MESSAGE.getValue(), customerOrder.value())
            .create();
    genericEvent.addTag(NIP01.createPubKeyTag(customerOrder.getContact().getPublicKey()));
    this.updateEvent(genericEvent);

    return this;
  }

  /**
   * Create or update a stall (kind 30017 per NIP-15).
   *
   * @param stall the stall definition
   * @return this instance for chaining
   */
  public NIP15 createCreateOrUpdateStallEvent(@NonNull Stall stall) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Kind.STALL_CREATE_OR_UPDATE.getValue(), stall.value()).create();
    genericEvent.addTag(NIP01.createIdentifierTag(stall.getId()));
    this.updateEvent(genericEvent);

    return this;
  }

  /**
   * Create or update a product (kind 30018 per NIP-15).
   *
   * @param product the product definition
   * @param categories optional list of hashtags/categories
   * @return this instance for chaining
   */
  public NIP15 createCreateOrUpdateProductEvent(@NonNull Product product, List<String> categories) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Kind.PRODUCT_CREATE_OR_UPDATE.getValue(), product.value()).create();
    genericEvent.addTag(NIP01.createIdentifierTag(product.getId()));

    if (categories != null && !categories.isEmpty()) {
      categories.forEach(
          category -> {
            genericEvent.addTag(NIP12.createHashtagTag(category));
          });
    }

    this.updateEvent(genericEvent);

    return this;
  }
}
