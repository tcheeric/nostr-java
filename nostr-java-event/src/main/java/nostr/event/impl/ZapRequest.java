package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.json.serializer.ZapRequestSerializer;
import nostr.event.tag.RelaysTag;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ZapRequestSerializer.class)
public class ZapRequest extends AbstractEventContent<ZapRequestEvent> {
  @JsonProperty
  private String id;

  @JsonProperty("relays")
  private RelaysTag relaysTag;

  @JsonProperty
  private Long amount;

  @JsonProperty("lnurl")
  private String lnUrl;

  public ZapRequest(@NonNull RelaysTag relaysTag, @NonNull Long amount, @NonNull String lnUrl) {
    this.relaysTag = relaysTag;
    this.amount = amount;
    this.lnUrl = lnUrl;
  }
}
