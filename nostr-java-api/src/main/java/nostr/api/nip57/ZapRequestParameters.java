package nostr.api.nip57;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

/**
 * Parameter object for building zap request events. Reduces long argument lists in {@link nostr.api.NIP57}.
 */
@Getter
@Builder
public final class ZapRequestParameters {

  private final Identity sender;
  @NonNull private final Long amount;
  @NonNull private final String lnUrl;
  private final String content;
  private final BaseTag addressTag;
  private final GenericEvent zappedEvent;
  private final PublicKey recipientPubKey;
  private final RelaysTag relaysTag;
  @Singular("relay") private final List<Relay> relays;

  public String contentOrDefault() {
    return content != null ? content : "";
  }

  public RelaysTag determineRelaysTag() {
    if (relaysTag != null) {
      return relaysTag;
    }
    if (relays != null && !relays.isEmpty()) {
      return new RelaysTag(relays);
    }
    throw new IllegalStateException("A relays tag or relay list is required to build zap requests");
  }

}
