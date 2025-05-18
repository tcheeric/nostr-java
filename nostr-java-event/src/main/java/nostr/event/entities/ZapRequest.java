package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.JsonContent;
import nostr.event.tag.RelaysTag;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapRequest implements JsonContent {
    //@JsonIgnore
    //private String id;

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
