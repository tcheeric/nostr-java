/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.entities.CustomerOrder;
import nostr.event.entities.PaymentRequest;
import nostr.event.entities.Product;
import nostr.event.entities.Stall;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.util.List;

/**
 * @author eric
 */
public class NIP15 extends EventNostr {

    public NIP15(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * @param paymentRequest
     * @param customerOrder
     */
    public NIP15 createMerchantRequestPaymentEvent(@NonNull PaymentRequest paymentRequest, @NonNull CustomerOrder customerOrder) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.ENCRYPTED_DIRECT_MESSAGE, paymentRequest.value()).create();
        genericEvent.addTag(NIP01.createPubKeyTag(customerOrder.getContact().getPublicKey()));
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * @param customerOrder
     * @return
     */
    public NIP15 createCustomerOrderEvent(@NonNull CustomerOrder customerOrder) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.ENCRYPTED_DIRECT_MESSAGE, customerOrder.value()).create();
        genericEvent.addTag(NIP01.createPubKeyTag(customerOrder.getContact().getPublicKey()));
        this.updateEvent(genericEvent);

        return this;
    }

    /**
     * @param stall
     * @return
     */
    public NIP15 createCreateOrUpdateStallEvent(@NonNull Stall stall) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.SET_STALL, stall.value()).create();
        genericEvent.addTag(NIP01.createIdentifierTag(stall.getId()));
        this.updateEvent(genericEvent);

        return this;
    }

    /**
     * @param product
     * @param categories
     * @return
     */
    public NIP15 createCreateOrUpdateProductEvent(@NonNull Product product, List<String> categories) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.SET_PRODUCT, product.value()).create();
        genericEvent.addTag(NIP01.createIdentifierTag(product.getId()));

        if (categories != null && !categories.isEmpty()) {
            categories.forEach(category -> {
                genericEvent.addTag(NIP12.createHashtagTag(category));
            });
        }

        this.updateEvent(genericEvent);

        return this;
    }
}
