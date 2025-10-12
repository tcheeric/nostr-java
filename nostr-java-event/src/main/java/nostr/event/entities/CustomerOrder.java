package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import nostr.base.PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class CustomerOrder extends NIP15Content.CheckoutContent {

  @JsonProperty private final String id;

  @JsonProperty private String name;

  @JsonProperty private String address;

  @JsonProperty private String message;

  @JsonProperty private Contact contact;

  @JsonProperty private List<Item> items;

  @JsonProperty("shipping_id")
  private String shippingId;

  public CustomerOrder() {
    this.items = new ArrayList<>();
    this.id = UUID.randomUUID().toString();
  }

  @Data
  public static class Contact {

    @JsonProperty("nostr")
    private final PublicKey publicKey;

    @JsonProperty private String phone;

    @JsonProperty private String email;

    public Contact(@NonNull PublicKey publicKey) {
      this.publicKey = publicKey;
    }
  }

  @Data
  @NoArgsConstructor
  public static class Item {

    @JsonProperty private Product product;

    @JsonProperty private int quantity;
  }
}
