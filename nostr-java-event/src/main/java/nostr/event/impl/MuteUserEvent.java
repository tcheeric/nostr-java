package nostr.event.impl;

import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author guilhermegps
 */
@Event(name = "Mute User on Channel", nip = 28)
@NoArgsConstructor
public class MuteUserEvent extends GenericEvent {

  public MuteUserEvent(PublicKey pubKey, List<BaseTag> baseTagList, String content) {
    super(pubKey, Kind.MUTE_USER, baseTagList, content);
  }

  public PublicKey getMutedUser() {
    return requireTagInstance(PubKeyTag.class).getPublicKey();
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    requireTagInstance(PubKeyTag.class);
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.MUTE_USER.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.MUTE_USER.getValue());
    }
  }
}
