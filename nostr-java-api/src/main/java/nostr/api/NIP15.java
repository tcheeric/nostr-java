/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP15Impl;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.impl.CustomerOrderEvent.Customer;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.MerchantRequestPaymentEvent.Payment;
import nostr.event.impl.NostrMarketplaceEvent.Product;
import nostr.id.Identity;

import java.util.List;

/**
 * @author eric
 */
public class NIP15<T extends GenericEvent> extends EventNostr<T> {

    public NIP15(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * @param payment
     * @param customer
     */
    public NIP15<T> createMerchantRequestPaymentEvent(@NonNull Payment payment, @NonNull Customer customer) {
        var factory = new NIP15Impl.MerchantRequestPaymentEventFactory(getSender(), customer, payment);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     *
     * @param customer
     * @return
     */
    public NIP15<T> createCustomerOrderEvent(@NonNull Customer customer) {
        var factory = new NIP15Impl.CustomerOrderEventFactory(getSender(), customer);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     *
     * @param stall
     * @return
     */
    public NIP15<T> createCreateOrUpdateStallEvent(@NonNull Stall stall) {
        var factory = new NIP15Impl.CreateOrUpdateStallEventFactory(getSender(), stall);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     *
     * @param product
     * @param categories
     * @return
     */
    public NIP15<T> createCreateOrUpdateProductEvent(@NonNull Product product, List<String> categories) {
        var factory = new NIP15Impl.CreateOrUpdateProductEventFactory(getSender(), product, categories);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }
}
