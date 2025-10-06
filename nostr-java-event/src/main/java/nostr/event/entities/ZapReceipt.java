package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.JsonContent;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapReceipt implements JsonContent {
  
  @JsonProperty private final String bolt11;

  @JsonProperty private final String descriptionSha256;

  @JsonProperty private final String preimage;

  public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256, String preimage) {
    this.descriptionSha256 = descriptionSha256;
    this.bolt11 = bolt11;
    this.preimage = preimage;
  }

  public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256) {
    this(bolt11, descriptionSha256, null);
  }
}
