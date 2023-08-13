/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
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
    public static VerifyPaymentOrShippedEvent createVerifyPaymentOrShippedEvent(Customer customer, PaymentShipmentStatus status) {
        return new VerifyPaymentOrShippedEventFactory(status, customer).create();
    }
    
    /**
     * 
     * @param tags
     * @param customer
     * @param status
     * @return 
     */
    public static VerifyPaymentOrShippedEvent createVerifyPaymentOrShippedEvent(List<BaseTag> tags, Customer customer, PaymentShipmentStatus status) {
        return new VerifyPaymentOrShippedEventFactory(tags, status, customer).create();
    }
    
    /**
     * 
     * @param payment
     * @param customer
     * @return 
     */
    public static MerchantRequestPaymentEvent createMerchantRequestPaymentEvent(Payment payment, Customer customer) {
        return new MerchantRequestPaymentEventFactory(customer, payment).create();
    }

    /**
     * 
     * @param customer
     * @return 
     */
    public static CustomerOrderEvent createCustomerOrderEvent(Customer customer) {
        return new CustomerOrderEventFactory(customer).create();
    }
    
    /**
     * 
     * @param stall
     * @return 
     */
    public static CreateOrUpdateStallEvent createCreateOrUpdateStallEvent(Stall stall) {
        return new CreateOrUpdateStallEventFactory(stall).create();
    }
    
    /**
     * 
     * @param product
     * @param categories
     * @return 
     */
    public static CreateOrUpdateProductEvent createCreateOrUpdateProductEvent(Product product, List<String> categories) {
        return new CreateOrUpdateProductEventFactory(product, categories).create();
    }
}
