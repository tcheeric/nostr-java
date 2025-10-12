package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP01Event;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Text Note")
@NoArgsConstructor
public class TextNoteEvent extends NIP01Event {

  public TextNoteEvent(
      @NonNull PublicKey pubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(pubKey, Kind.TEXT_NOTE, tags, content);
  }

  public List<PubKeyTag> getRecipientPubkeyTags() {
    return nostr.event.filter.Filterable.getTypeSpecificTags(PubKeyTag.class, this);
  }

  public List<PublicKey> getRecipients() {
    return nostr.event.filter.Filterable.getTypeSpecificTags(PubKeyTag.class, this).stream()
        .map(PubKeyTag::getPublicKey)
        .toList();
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.TEXT_NOTE.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.TEXT_NOTE.getValue());
    }
  }
}
