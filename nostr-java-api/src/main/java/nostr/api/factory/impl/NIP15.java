/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.CreateOrUpdateProductEvent;
import nostr.event.impl.CreateOrUpdateStallEvent;
import nostr.event.impl.CustomerOrderEvent;
import nostr.event.impl.MerchantRequestPaymentEvent;
import nostr.event.impl.NostrMarketplaceEvent;
import nostr.event.impl.VerifyPaymentOrShippedEvent;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;

/**
 *
 * @author eric
 */
public class NIP15 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class VerifyPaymentOrShippedEventFactory extends EventFactory<VerifyPaymentOrShippedEvent> {

        private final CustomerOrderEvent.Customer customer;
        private final VerifyPaymentOrShippedEvent.PaymentShipmentStatus status;

        public VerifyPaymentOrShippedEventFactory(VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, CustomerOrderEvent.Customer customer) {
            super(status.toString());
            this.status = status;
            this.customer = customer;
        }

        public VerifyPaymentOrShippedEventFactory(List<BaseTag> tags, VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, CustomerOrderEvent.Customer customer) {
            super(tags, status.toString());
            this.status = status;
            this.customer = customer;
        }

        @Deprecated
        public VerifyPaymentOrShippedEventFactory(PublicKey sender, VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, CustomerOrderEvent.Customer customer) {
            super(sender, status.toString());
            this.status = status;
            this.customer = customer;
        }

        @Override
        public VerifyPaymentOrShippedEvent create() {
            return new VerifyPaymentOrShippedEvent(getSender(), customer, status);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class MerchantRequestPaymentEventFactory extends EventFactory<MerchantRequestPaymentEvent> {

        private final MerchantRequestPaymentEvent.Payment payment;
        private final CustomerOrderEvent.Customer customer;

        public MerchantRequestPaymentEventFactory(CustomerOrderEvent.Customer customer, MerchantRequestPaymentEvent.Payment payment) {
            super(payment.toString());
            this.payment = payment;
            this.customer = customer;
        }

        @Deprecated
        public MerchantRequestPaymentEventFactory(PublicKey sender, CustomerOrderEvent.Customer customer, MerchantRequestPaymentEvent.Payment payment) {
            super(sender, payment.toString());
            this.payment = payment;
            this.customer = customer;
        }

        @Override
        public MerchantRequestPaymentEvent create() {
            return new MerchantRequestPaymentEvent(getSender(), this.customer, payment);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CustomerOrderEventFactory extends EventFactory<CustomerOrderEvent> {

        private final CustomerOrderEvent.Customer customer;

        public CustomerOrderEventFactory(@NonNull CustomerOrderEvent.Customer customer) {
            super(customer.toString());
            this.customer = customer;
        }

        @Deprecated
        public CustomerOrderEventFactory(PublicKey sender, @NonNull CustomerOrderEvent.Customer customer) {
            super(sender, customer.toString());
            this.customer = customer;
        }

        @Override
        public CustomerOrderEvent create() {
            return new CustomerOrderEvent(getSender(), customer);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CreateOrUpdateStallEventFactory extends EventFactory<CreateOrUpdateStallEvent> {

        private final CreateOrUpdateStallEvent.Stall stall;

        public CreateOrUpdateStallEventFactory(@NonNull CreateOrUpdateStallEvent.Stall stall) {
            super(stall.toString());
            this.stall = stall;
        }

        @Deprecated
        public CreateOrUpdateStallEventFactory(PublicKey sender, @NonNull CreateOrUpdateStallEvent.Stall stall) {
            super(sender, stall.toString());
            this.stall = stall;
        }

        @Override
        public CreateOrUpdateStallEvent create() {
            return new CreateOrUpdateStallEvent(getSender(), new ArrayList<>(), stall);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CreateOrUpdateProductEventFactory extends EventFactory<CreateOrUpdateProductEvent> {

        private final NostrMarketplaceEvent.Product product;
        private final List<String> categories;

        public CreateOrUpdateProductEventFactory(@NonNull NostrMarketplaceEvent.Product product, List<String> categories) {
            super(product.toString());
            this.product = product;
            this.categories = categories;
        }

        @Deprecated
        public CreateOrUpdateProductEventFactory(PublicKey sender, @NonNull NostrMarketplaceEvent.Product product, List<String> categories) {
            super(sender, product.toString());
            this.product = product;
            this.categories = categories;
        }

        @Override
        public CreateOrUpdateProductEvent create() {
            var event = new CreateOrUpdateProductEvent(getSender(), new ArrayList<>(), product);
            event.addTag(new IdentifierTag(product.getId()));
            if (categories != null) {
                categories.forEach(c -> event.addTag(new HashtagTag(c)));
            }

            return event;
        }

    }

    public static class Kinds {

        public static final Integer KIND_SET_STALL = 30017;
        public static final Integer KIND_SET_PRODUCT = 30018;
    }

}
