
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.json.serializer.ZapRequestSerializer;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapRequestEvent", nip = 57)
public class ZapRequestEvent extends GenericEvent {
  private final ZapRequest zapRequest;

  public ZapRequestEvent(@NonNull PublicKey pubKey, @NonNull PublicKey recipientPubKey, List<BaseTag> tags, String content, @NonNull ZapRequest zapRequest) {
    super(pubKey, Kind.ZAP_REQUEST, tags, content);
    super.addTag(new PubKeyTag(recipientPubKey));
    this.zapRequest = zapRequest;
  }

  public ZapRequestEvent(@NonNull String pubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<String> relaysTags) {
    super(new PublicKey(pubKey), Kind.ZAP_REQUEST, tags, content);
    super.addTag(new PubKeyTag(new PublicKey(recipientPubKey)));
    this.zapRequest = new ZapRequest(new RelaysTag(relaysTags), amount, lnUrl);
  }

  public ZapRequestEvent(@NonNull String pubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull String... relaysTags) {
    this(pubKey, recipientPubKey, tags, content, amount, lnUrl, List.of(relaysTags));
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ZapRequestSerializer.class)
  public static class ZapRequest extends AbstractEventContent<ZapRequestEvent> {
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
}
