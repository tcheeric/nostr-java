package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author eric
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "", nip = 15)
public class MerchantRequestPaymentEventNick extends EventDecorator {
  private final Payment payment;

  public MerchantRequestPaymentEventNick(GenericEventNick genericEvent, @NonNull Payment payment) {
    super(genericEvent);
    this.payment = payment;
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  @ToString(callSuper = true)
  public static class Payment extends AbstractEventContent<MerchantRequestPaymentEventNick> {

    @JsonProperty
    private final String id;

    @JsonProperty
    private CheckoutEventNick.MessageType type;

    @JsonProperty
    private String message;

    @JsonProperty("payment_options")
    private List<PaymentOptions> paymentOptions;

    public Payment() {
      this.paymentOptions = new ArrayList<>();
      this.id = UUID.randomUUID().toString();
    }

    @Data
    @NoArgsConstructor
    public static class PaymentOptions {

      public enum Type {
        URL,
        BTC,
        LN,
        LNURL;

        @JsonValue
        public String getValue() {
          return name().toLowerCase();
        }
      }

      @JsonProperty
      private Type type;

      @JsonProperty
      private String link;
    }
  }
}
