
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;

import java.util.Arrays;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapRequestEvent", nip = 57)
public class ZapRequestEvent extends GenericEvent {
  @JsonIgnore
  private final ZapRequest zapRequest;

  public ZapRequestEvent(@NonNull PublicKey senderPubKey, @NonNull PubKeyTag recipientPubKey, List<BaseTag> tags, String content, @NonNull ZapRequest zapRequest) {
    super(senderPubKey, Kind.ZAP_REQUEST, tags, content);
    super.addTag(recipientPubKey);
    this.zapRequest = zapRequest;
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull PubKeyTag recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull RelaysTag relaysTag) {
    this(new PublicKey(senderPubKey), recipientPubKey, tags, content, new ZapRequest(relaysTag, amount, lnUrl));
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<Relay> relays) {
    this(senderPubKey, new PubKeyTag(new PublicKey(recipientPubKey)), tags, content, amount, lnUrl, new RelaysTag(relays));
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull String... relays) {
    this(senderPubKey, recipientPubKey, tags, content, amount, lnUrl, Arrays.stream(relays).map(Relay::new).toList());
  }
}
