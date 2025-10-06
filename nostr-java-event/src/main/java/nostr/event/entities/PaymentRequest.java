package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class PaymentRequest extends NIP15Content.CheckoutContent {

  @JsonProperty private final String id;

  @JsonProperty private String message;

  @JsonProperty("payment_options")
  private final List<PaymentOptions> paymentOptions;

  public PaymentRequest() {
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

    @JsonProperty private Type type;

    @JsonProperty private String link;
  }
}
