/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP15.CreateOrUpdateProductEventFactory;
import nostr.api.factory.impl.NIP15.CreateOrUpdateStallEventFactory;
import nostr.api.factory.impl.NIP15.CustomerOrderEventFactory;
import nostr.api.factory.impl.NIP15.MerchantRequestPaymentEventFactory;
import nostr.api.factory.impl.NIP15.VerifyPaymentOrShippedEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.CreateOrUpdateProductEvent;
import nostr.event.impl.CreateOrUpdateStallEvent;
import nostr.event.impl.CreateOrUpdateStallEvent.Stall;
import nostr.event.impl.CustomerOrderEvent;
import nostr.event.impl.CustomerOrderEvent.Customer;
import nostr.event.impl.MerchantRequestPaymentEvent;
import nostr.event.impl.MerchantRequestPaymentEvent.Payment;
import nostr.event.impl.NostrMarketplaceEvent.Product;
import nostr.event.impl.VerifyPaymentOrShippedEvent;
import nostr.event.impl.VerifyPaymentOrShippedEvent.PaymentShipmentStatus;

/**
 *
 * @author eric
 */
public class NIP15 extends Nostr {

    /**
     * 
     * @param customer
     * @param status
     * @return 
     */
    public static VerifyPaymentOrShippedEvent createVerifyPaymentOrShippedEvent(@NonNull Customer customer, @NonNull PaymentShipmentStatus status) {
        return new VerifyPaymentOrShippedEventFactory(status, customer).create();
    }
    
    /**
     * 
     * @param tags
     * @param customer
     * @param status
     * @return 
     */
    public static VerifyPaymentOrShippedEvent createVerifyPaymentOrShippedEvent(@NonNull List<BaseTag> tags, @NonNull Customer customer, @NonNull PaymentShipmentStatus status) {
        return new VerifyPaymentOrShippedEventFactory(tags, status, customer).create();
    }
    
    /**
     * 
     * @param payment
     * @param customer
     * @return 
     */
    public static MerchantRequestPaymentEvent createMerchantRequestPaymentEvent(@NonNull Payment payment, @NonNull Customer customer) {
        return new MerchantRequestPaymentEventFactory(customer, payment).create();
    }

    /**
     * 
     * @param customer
     * @return 
     */
    public static CustomerOrderEvent createCustomerOrderEvent(@NonNull Customer customer) {
        return new CustomerOrderEventFactory(customer).create();
    }
    
    /**
     * 
     * @param stall
     * @return 
     */
    public static CreateOrUpdateStallEvent createCreateOrUpdateStallEvent(@NonNull Stall stall) {
        return new CreateOrUpdateStallEventFactory(stall).create();
    }
    
    /**
     * 
     * @param product
     * @param categories
     * @return 
     */
    public static CreateOrUpdateProductEvent createCreateOrUpdateProductEvent(@NonNull Product product, List<String> categories) {
        return new CreateOrUpdateProductEventFactory(product, categories).create();
    }
}
