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
import nostr.event.Kind;
import nostr.event.impl.*;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP15Nick {

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class VerifyPaymentOrShippedEventFactory extends EventFactory<VerifyPaymentOrShippedEventNick> {

    private final CustomerOrderEventNick.Customer customer;
    private final VerifyPaymentOrShippedEventNick.PaymentShipmentStatus status;

    public VerifyPaymentOrShippedEventFactory(@NonNull VerifyPaymentOrShippedEventNick.PaymentShipmentStatus status, @NonNull CustomerOrderEventNick.Customer customer) {
      super(status.toString());
      this.status = status;
      this.customer = customer;
    }

    public VerifyPaymentOrShippedEventFactory(@NonNull List<BaseTag> tags, VerifyPaymentOrShippedEventNick.PaymentShipmentStatus status, @NonNull CustomerOrderEventNick.Customer customer) {
      super(tags, status.toString());
      this.status = status;
      this.customer = customer;
    }

    @Deprecated
    public VerifyPaymentOrShippedEventFactory(@NonNull Identity sender, @NonNull VerifyPaymentOrShippedEventNick.PaymentShipmentStatus status, @NonNull CustomerOrderEventNick.Customer customer) {
      super(sender, status.toString());
      this.status = status;
      this.customer = customer;
    }

    @Override
    public VerifyPaymentOrShippedEventNick create() {
      return new VerifyPaymentOrShippedEventNick(
          new CheckoutEventNick(
              new DirectMessageEventNick(
                  new GenericEventImpl(
                      getSender()
                  )
              )
          ),
          customer, status);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class MerchantRequestPaymentEventFactory extends EventFactory<MerchantRequestPaymentEventNick> {

    private final MerchantRequestPaymentEventNick.Payment payment;
    private final CustomerOrderEventNick.Customer customer;

    public MerchantRequestPaymentEventFactory(@NonNull CustomerOrderEventNick.Customer customer, @NonNull MerchantRequestPaymentEventNick.Payment payment) {
      super(payment.toString());
      this.payment = payment;
      this.customer = customer;
    }

    @Deprecated
    public MerchantRequestPaymentEventFactory(@NonNull Identity sender, CustomerOrderEventNick.Customer customer, @NonNull MerchantRequestPaymentEventNick.Payment payment) {
      super(sender, payment.toString());
      this.payment = payment;
      this.customer = customer;
    }

    @Override
    public MerchantRequestPaymentEventNick create() {
      return new MerchantRequestPaymentEventNick(
          new CustomerOrderEventNick(
              new GenericEventImpl(getSender()), customer), payment);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class CustomerOrderEventFactory extends EventFactory<CustomerOrderEventNick> {

    private final CustomerOrderEventNick.Customer customer;

    public CustomerOrderEventFactory(@NonNull CustomerOrderEventNick.Customer customer) {
      super(customer.toString());
      this.customer = customer;
    }

    @Deprecated
    public CustomerOrderEventFactory(Identity identity, @NonNull CustomerOrderEventNick.Customer customer) {
      super(identity, customer.toString());
      this.customer = customer;
    }

    @Override
    public CustomerOrderEventNick create() {
      return new CustomerOrderEventNick(new GenericEventImpl(getSender()), customer);
    }

  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class CreateOrUpdateStallEventFactory extends EventFactory<CreateOrUpdateStallEventNick> {

    private final CreateOrUpdateStallEventNick.Stall stall;

    public CreateOrUpdateStallEventFactory(@NonNull CreateOrUpdateStallEventNick.Stall stall) {
      super(stall.toString());
      this.stall = stall;
    }

    @Override
    public CreateOrUpdateStallEventNick create() {
      GenericEventImpl genericEvent = new GenericEventImpl(getSender());
      ReplaceableEventNick replaceableEventNick = new ReplaceableEventNick(genericEvent, Kind.KIND_SET_STALL);
      ParameterizedReplaceableEventNick parameterizedReplaceableEventNick = new ParameterizedReplaceableEventNick(replaceableEventNick);
      NostrMarketplaceEventNick nostrMarketplaceEventNick = new NostrMarketplaceEventNick(parameterizedReplaceableEventNick);
      return new CreateOrUpdateStallEventNick(nostrMarketplaceEventNick, new ArrayList<>(), stall);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class CreateOrUpdateProductEventFactory extends EventFactory<CreateOrUpdateProductEventNick> {

    private final ProductNick product;
    private final List<String> categories;

    public CreateOrUpdateProductEventFactory(@NonNull ProductNick product, List<String> categories) {
      super(product.toString());
      this.product = product;
      this.categories = categories;
    }

    @Deprecated
    public CreateOrUpdateProductEventFactory(Identity identity, @NonNull ProductNick product, List<String> categories) {
      super(identity, product.toString());
      this.product = product;
      this.categories = categories;
    }

    @Override
    public CreateOrUpdateProductEventNick create() {
      return new CreateOrUpdateProductEventNick(
          new NostrMarketplaceEventNick(
              new ParameterizedReplaceableEventNick(
                  new ReplaceableEventNick(
                      new GenericEventImpl(getSender()), Kind.KIND_SET_PRODUCT))), product, categories);
    }
  }
}
