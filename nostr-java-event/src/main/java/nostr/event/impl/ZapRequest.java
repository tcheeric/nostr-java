package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.tag.RelaysTag;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapRequest extends AbstractEventContent<ZapRequestEvent> {
  @JsonIgnore
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
