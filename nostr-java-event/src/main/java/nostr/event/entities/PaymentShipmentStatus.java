package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class PaymentShipmentStatus extends NIP15Content.CheckoutContent {

  @JsonProperty private final String id;

  @JsonProperty private String message;

  @JsonProperty private boolean paid;

  @JsonProperty private boolean shipped;

  public PaymentShipmentStatus() {
    this.id = UUID.randomUUID().toString();
  }
}
