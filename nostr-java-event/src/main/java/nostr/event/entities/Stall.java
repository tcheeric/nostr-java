package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class Stall extends NIP15Content.MerchantContent {

  @JsonProperty private final String id;

  @JsonProperty private String name;

  @JsonProperty private String description;

  @JsonProperty private String currency;

  @JsonProperty private Shipping shipping;

  public Stall() {
    this.id = UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).substring(0, 64);
  }

  @Data
  public static class Shipping {

    @JsonProperty private final String id;

    @JsonProperty private String name;

    @JsonProperty private Float cost;

    @JsonProperty private List<String> countries;

    public Shipping() {
      this.countries = new ArrayList<>();
      this.id = UUID.randomUUID().toString();
    }
  }
}
