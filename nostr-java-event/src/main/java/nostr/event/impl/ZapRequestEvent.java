package nostr.event.impl;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.ZapRequest;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapRequestEvent", nip = 57)
@NoArgsConstructor
public class ZapRequestEvent extends GenericEvent {

  public ZapRequestEvent(
      @NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(recipientPubKey, Kind.ZAP_REQUEST, tags, content);
  }

  public ZapRequest getZapRequest() {
    RelaysTag relaysTag =
        nostr.event.filter.Filterable.requireTagOfTypeWithCode(RelaysTag.class, "relays", this);
    String amount =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "amount", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();
    String lnurl =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "lnurl", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();

    return new ZapRequest(relaysTag, Long.parseLong(amount), lnurl);
  }

  public PublicKey getRecipientKey() {
    PubKeyTag p =
        nostr.event.filter.Filterable.requireTagOfTypeWithCode(
            PubKeyTag.class, "p", this, "Recipient public key not found in tags");
    return p.getPublicKey();
  }

  public String getEventId() {
    return nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "e", this)
        .map(tag -> tag.getAttributes().get(0).value().toString())
        .orElse(null);
  }

  public List<Relay> getRelays() {
    ZapRequest zapRequest = getZapRequest();
    return zapRequest.getRelaysTag() != null ? zapRequest.getRelaysTag().getRelays() : null;
  }

  public String getLnUrl() {
    ZapRequest zapRequest = getZapRequest();
    return zapRequest.getLnUrl();
  }

  public Long getAmount() {
    ZapRequest zapRequest = getZapRequest();
    return zapRequest.getAmount();
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate `tags` field
    // Check for required tags
    boolean hasRecipientTag =
        nostr.event.filter.Filterable
            .firstTagOfTypeWithCode(PubKeyTag.class, "p", this)
            .isPresent();
    if (!hasRecipientTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include a `p` tag for the recipient's public key.");
    }

    boolean hasAmountTag =
        nostr.event.filter.Filterable
            .firstTagOfTypeWithCode(GenericTag.class, "amount", this)
            .isPresent();
    if (!hasAmountTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include an `amount` tag specifying the amount in millisatoshis.");
    }

    boolean hasLnUrlTag =
        nostr.event.filter.Filterable
            .firstTagOfTypeWithCode(GenericTag.class, "lnurl", this)
            .isPresent();
    if (!hasLnUrlTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include an `lnurl` tag containing the Lightning Network URL.");
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.ZAP_REQUEST.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.ZAP_REQUEST.getValue());
    }
  }
}
