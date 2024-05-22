
package nostr.event.impl;

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
import nostr.event.tag.ZapRequest;

import java.util.Arrays;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapRequestEvent", nip = 57)
public class ZapRequestEvent extends GenericEvent {
  private final ZapRequest zapRequest;

  public ZapRequestEvent(@NonNull PublicKey senderPubKey, @NonNull PublicKey recipientPubKey, List<BaseTag> tags, String content, @NonNull ZapRequest zapRequest) {
    super(senderPubKey, Kind.ZAP_REQUEST, tags, content);
    super.addTag(new PubKeyTag(recipientPubKey));
    this.zapRequest = zapRequest;
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull RelaysTag relaysTag) {
    super(new PublicKey(senderPubKey), Kind.ZAP_REQUEST, tags, content);
    super.addTag(new PubKeyTag(new PublicKey(recipientPubKey)));
    this.zapRequest = new ZapRequest(relaysTag, amount, lnUrl);
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<Relay> relays) {
    this(senderPubKey, recipientPubKey, tags, content, amount, lnUrl, new RelaysTag(relays));
  }

  public ZapRequestEvent(@NonNull String senderPubKey, @NonNull String recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull String... relays) {
    this(senderPubKey, recipientPubKey, tags, content, amount, lnUrl, Arrays.stream(relays).map(Relay::new).toList());
  }
}
