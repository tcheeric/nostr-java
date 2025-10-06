package nostr.event.impl;

import java.util.List;
import lombok.NoArgsConstructor;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

@NoArgsConstructor
public abstract class AbstractBaseNostrConnectEvent extends EphemeralEvent {
  public AbstractBaseNostrConnectEvent(
      PublicKey pubKey, List<BaseTag> baseTagList, String content) {
    super(pubKey, 24_133, baseTagList, content);
  }

  public PublicKey getActor() {
    var pTag =
        nostr.event.filter.Filterable.requireTagOfType(
            PubKeyTag.class, this, "Invalid `tags`: missing PubKeyTag (p)");
    return pTag.getPublicKey();
  }

  public void validate() {
    super.validate();

    // 1. p - tag validation
    nostr.event.filter.Filterable
        .firstTagOfType(PubKeyTag.class, this)
        .orElseThrow(
            () -> new AssertionError("Invalid `tags`: Must include at least one valid PubKeyTag."));
  }
}
