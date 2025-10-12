package nostr.event.impl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.util.List;

/**
 * @author squirrel
 */
@Event(name = "Canonical authentication event", nip = 42)
@NoArgsConstructor
public class CanonicalAuthenticationEvent extends EphemeralEvent {

  public CanonicalAuthenticationEvent(
      @NonNull PublicKey pubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(pubKey, Kind.CLIENT_AUTH, tags, content);
  }

  public String getChallenge() {
    return nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "challenge", this)
        .filter(tag -> !tag.getAttributes().isEmpty())
        .map(tag -> tag.getAttributes().get(0).value().toString())
        .orElse(null);
  }

  public Relay getRelay() {
    return nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "relay", this)
        .filter(tag -> !tag.getAttributes().isEmpty())
        .map(tag -> new Relay(tag.getAttributes().get(0).value().toString()))
        .orElse(null);
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Check 'challenge' tag
    nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "challenge", this)
        .filter(tag -> !tag.getAttributes().isEmpty())
        .orElseThrow(() -> new AssertionError("Missing or invalid `challenge` tag."));

    // Check 'relay' tag
    nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "relay", this)
        .filter(tag -> !tag.getAttributes().isEmpty())
        .orElseThrow(() -> new AssertionError("Missing or invalid `relay` tag."));
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.CLIENT_AUTH.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.CLIENT_AUTH.getValue());
    }
  }
}
