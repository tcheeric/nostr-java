package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP25Event;
import nostr.event.tag.EventTag;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Reactions", nip = 25)
@NoArgsConstructor
public class ReactionEvent extends NIP25Event {

  public ReactionEvent(PublicKey pubKey, List<BaseTag> tags, String content) {
    super(pubKey, Kind.REACTION, tags, content);
  }

  public String getReactedEventId() {
    return requireTagInstance(EventTag.class).getIdEvent();
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    requireTagInstance(EventTag.class);
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.REACTION.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.REACTION.getValue());
    }
  }
}
