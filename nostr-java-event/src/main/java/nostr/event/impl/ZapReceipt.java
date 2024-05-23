package nostr.event.impl;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.json.serializer.ZapReceiptSerializer;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ZapReceiptSerializer.class)
public class ZapReceipt extends AbstractEventContent<ZapReceiptEvent> {
  @JsonProperty
  private String id;

  @JsonProperty
  private String bolt11;

  @JsonProperty
  private String descriptionSha256;

  @JsonProperty
  private String preimage;

  public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256, String preimage) {
    this.descriptionSha256 = descriptionSha256;
    this.bolt11 = bolt11;
    this.preimage = preimage;
  }

  public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256) {
    this(bolt11, descriptionSha256, null);
  }
}