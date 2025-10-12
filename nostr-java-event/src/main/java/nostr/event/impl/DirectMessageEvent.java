package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.NIP04Event;
import nostr.event.tag.PubKeyTag;

import java.util.List;

/**
 * @author squirrel
 */
@NoArgsConstructor
@Event(name = "Encrypted Direct Message", nip = 4)
public class DirectMessageEvent extends NIP04Event {

  public DirectMessageEvent(PublicKey sender, List<BaseTag> tags, String content) {
    super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE, tags, content);
  }

  public DirectMessageEvent(
      @NonNull PublicKey sender, @NonNull PublicKey recipient, @NonNull String content) {
    super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE);
    this.setContent(content);
    this.addTag(PubKeyTag.builder().publicKey(recipient).build());
  }

  @Override
  protected void validateTags() {

    super.validateTags();

    // Validate `tags` field for recipient's public key
    boolean hasRecipientTag =
        !nostr.event.filter.Filterable.getTypeSpecificTags(PubKeyTag.class, this).isEmpty();
    if (!hasRecipientTag) {
      throw new AssertionError("Invalid `tags`: Must include a PubKeyTag for the recipient.");
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.ENCRYPTED_DIRECT_MESSAGE.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.ENCRYPTED_DIRECT_MESSAGE.getValue());
    }
  }
}
