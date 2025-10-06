package nostr.event.impl;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.ZapReceipt;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapReceiptEvent", nip = 57)
@NoArgsConstructor
public class ZapReceiptEvent extends GenericEvent {

  public ZapReceiptEvent(
      @NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
    super(recipientPubKey, Kind.ZAP_RECEIPT, tags, content);
  }

  public ZapReceipt getZapReceipt() {
    var bolt11 =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "bolt11", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();
    var description =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "description", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();
    var preimage =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "preimage", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();

    return new ZapReceipt(bolt11, description, preimage);
  }

  public String getBolt11() {
    ZapReceipt zapReceipt = getZapReceipt();
    return zapReceipt.getBolt11();
  }

  public String getDescriptionSha256() {
    ZapReceipt zapReceipt = getZapReceipt();
    return zapReceipt.getDescriptionSha256();
  }

  public String getPreimage() {
    ZapReceipt zapReceipt = getZapReceipt();
    return zapReceipt.getPreimage();
  }

  public PublicKey getRecipient() {
    PubKeyTag recipientPubKeyTag =
        nostr.event.filter.Filterable.requireTagOfTypeWithCode(PubKeyTag.class, "p", this);
    return recipientPubKeyTag.getPublicKey();
  }

  public PublicKey getSender() {
    return nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(PubKeyTag.class, "P", this)
        .map(PubKeyTag::getPublicKey)
        .orElse(null);
  }

  public String getEventId() {
    return nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "e", this)
        .map(tag -> tag.getAttributes().get(0).value().toString())
        .orElse(null);
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate `tags` field
    // Check for required tags
    nostr.event.filter.Filterable.requireTagOfTypeWithCode(PubKeyTag.class, "p", this);
    nostr.event.filter.Filterable.requireTagOfTypeWithCode(GenericTag.class, "bolt11", this);
    nostr.event.filter.Filterable.requireTagOfTypeWithCode(GenericTag.class, "description", this);
    nostr.event.filter.Filterable.requireTagOfTypeWithCode(GenericTag.class, "preimage", this);
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.ZAP_RECEIPT.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.ZAP_RECEIPT.getValue());
    }
  }
}
