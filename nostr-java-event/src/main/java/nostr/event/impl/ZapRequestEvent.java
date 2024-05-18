
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP57Event;
import nostr.event.json.serializer.ZapRequestSerializer;
import nostr.event.tag.RelaysTag;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapRequestEvent", nip = 57)
public class ZapRequestEvent extends NIP57Event {
  private final ZapRequest zapRequest;

  public ZapRequestEvent(PublicKey pubKey, List<BaseTag> tags, String content, ZapRequest zapRequest) {
    super(pubKey, Kind.ZAP_REQUEST, tags, content);
    this.zapRequest = zapRequest;
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ZapRequestSerializer.class)
  public static class ZapRequest extends AbstractEventContent<ZapRequestEvent> {
    @JsonProperty
    private String id;

    @JsonProperty
    private RelaysTag relaysTag;

    @JsonProperty
    private Integer amount;

    @JsonProperty("lnurl")
    private String lnUrl;

    @JsonProperty("p")
    private String recipientPubKey;

    public ZapRequest(@NonNull RelaysTag relaysTag, @NonNull Integer amount, @NonNull String lnUrl, @NonNull PublicKey recipientPubKey) {
      this.relaysTag = relaysTag;
      this.amount = amount;
      this.lnUrl = lnUrl;
      this.recipientPubKey = recipientPubKey.toBech32String();
    }
  }
}
