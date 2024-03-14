/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.*;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.id.IIdentity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class NIP15Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class VerifyPaymentOrShippedEventFactory extends EventFactory<VerifyPaymentOrShippedEvent> {

        private final CustomerOrderEvent.Customer customer;
        private final VerifyPaymentOrShippedEvent.PaymentShipmentStatus status;

        public VerifyPaymentOrShippedEventFactory(@NonNull VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, @NonNull CustomerOrderEvent.Customer customer) {
            super(status.toString());
            this.status = status;
            this.customer = customer;
        }

        public VerifyPaymentOrShippedEventFactory(@NonNull List<BaseTag> tags, VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, @NonNull CustomerOrderEvent.Customer customer) {
            super(tags, status.toString());
            this.status = status;
            this.customer = customer;
        }

        @Deprecated
        public VerifyPaymentOrShippedEventFactory(@NonNull IIdentity sender, @NonNull VerifyPaymentOrShippedEvent.PaymentShipmentStatus status, @NonNull CustomerOrderEvent.Customer customer) {
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

        public MerchantRequestPaymentEventFactory(@NonNull CustomerOrderEvent.Customer customer, @NonNull MerchantRequestPaymentEvent.Payment payment) {
            super(payment.toString());
            this.payment = payment;
            this.customer = customer;
        }

        public MerchantRequestPaymentEventFactory(@NonNull IIdentity sender, CustomerOrderEvent.Customer customer, @NonNull MerchantRequestPaymentEvent.Payment payment) {
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

        public CustomerOrderEventFactory(IIdentity identity, @NonNull CustomerOrderEvent.Customer customer) {
            super(identity, customer.toString());
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

        public CreateOrUpdateStallEventFactory(IIdentity identity, @NonNull CreateOrUpdateStallEvent.Stall stall) {
            super(identity, stall.toString());
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

        public CreateOrUpdateProductEventFactory(IIdentity identity, @NonNull NostrMarketplaceEvent.Product product, List<String> categories) {
            super(identity, product.toString());
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
}
