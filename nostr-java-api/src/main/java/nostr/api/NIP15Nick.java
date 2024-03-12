/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP15Nick.CreateOrUpdateProductEventFactory;
import nostr.api.factory.impl.NIP15Nick.CreateOrUpdateStallEventFactory;
import nostr.api.factory.impl.NIP15Nick.CustomerOrderEventFactory;
import nostr.api.factory.impl.NIP15Nick.VerifyPaymentOrShippedEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.*;
import nostr.event.impl.CreateOrUpdateStallEventNick.Stall;
import nostr.event.impl.CustomerOrderEventNick.Customer;
import nostr.event.impl.VerifyPaymentOrShippedEventNick.PaymentShipmentStatus;

import java.util.List;

/**
 * @author eric
 */
public class NIP15Nick extends Nostr {

  /**
   * @param customer
   * @param status
   * @return
   */
  public static VerifyPaymentOrShippedEventNick createVerifyPaymentOrShippedEvent(@NonNull Customer customer, @NonNull PaymentShipmentStatus status) {
    return new VerifyPaymentOrShippedEventFactory(status, customer).create();
  }

  /**
   * @param tags
   * @param customer
   * @param status
   * @return
   */
  public static VerifyPaymentOrShippedEventNick createVerifyPaymentOrShippedEvent(@NonNull List<BaseTag> tags, @NonNull Customer customer, @NonNull PaymentShipmentStatus status) {
    return new VerifyPaymentOrShippedEventFactory(tags, status, customer).create();
  }

  /**
   * @param payment
   * @param customer
   * @return
   */
  public static MerchantRequestPaymentEventNick createMerchantRequestPaymentEvent(@NonNull MerchantRequestPaymentEventNick.Payment payment, @NonNull Customer customer) {
    return new nostr.api.factory.impl.NIP15Nick.MerchantRequestPaymentEventFactory(customer, payment).create();
  }

  /**
   * @param customer
   * @return
   */
  public static CustomerOrderEventNick createCustomerOrderEvent(@NonNull Customer customer) {
    return new CustomerOrderEventFactory(customer).create();
  }

  /**
   * @param stall
   * @return
   */
  public static CreateOrUpdateStallEventNick createCreateOrUpdateStallEvent(@NonNull Stall stall) {
    return new CreateOrUpdateStallEventFactory(stall).create();
  }

  /**
   * @param product
   * @param categories
   * @return
   */
  public static CreateOrUpdateProductEventNick createCreateOrUpdateProductEvent(@NonNull ProductNick product, List<String> categories) {
    return new CreateOrUpdateProductEventFactory(product, categories).create();
  }
}
