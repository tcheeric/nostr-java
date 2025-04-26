
package nostr.event.impl;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.entities.ZapReceipt;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapReceiptEvent", nip = 57)
@NoArgsConstructor
public class ZapReceiptEvent extends GenericEvent {

    public ZapReceiptEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(recipientPubKey, Kind.ZAP_RECEIPT, tags, content);
    }

    public ZapReceipt getZapReceipt() {
        GenericTag preimageTag = getTag("preimage");
        GenericTag descriptionTag = getTag("description");
        GenericTag bolt11Tag = getTag("bolt11");

        return new ZapReceipt(
                bolt11Tag.getAttributes().get(0).getValue().toString(),
                descriptionTag.getAttributes().get(0).getValue().toString(),
                preimageTag.getAttributes().get(0).getValue().toString()
        );
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
        GenericTag recipientTag = getTag("p");
        PubKeyTag recipientPubKeyTag = GenericTag.convert(recipientTag, PubKeyTag.class);
        return recipientPubKeyTag.getPublicKey();
    }

    public PublicKey getSender() {
        GenericTag senderTag = getTag("P");
        if (senderTag == null) {
            return null;
        }
        PubKeyTag senderPubKeyTag = GenericTag.convert(senderTag, PubKeyTag.class);
        return senderPubKeyTag.getPublicKey();
    }

    public String getEventId() {
        GenericTag eventTag = getTag("e");
        if (eventTag == null) {
            return null;
        }
        return eventTag.getAttributes().get(0).getValue().toString();
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate `tags` field
        // Check for required tags
        boolean hasRecipientTag = this.getTags().stream().anyMatch(tag -> "p".equals(tag.getCode()));
        if (!hasRecipientTag) {
            throw new AssertionError("Invalid `tags`: Must include a `p` tag for the recipient's public key.");
        }

        boolean hasBolt11Tag = this.getTags().stream().anyMatch(tag -> "bolt11".equals(tag.getCode()));
        if (!hasBolt11Tag) {
            throw new AssertionError("Invalid `tags`: Must include a `bolt11` tag for the Lightning invoice.");
        }

        boolean hasDescriptionTag = this.getTags().stream().anyMatch(tag -> "description".equals(tag.getCode()));
        if (!hasDescriptionTag) {
            throw new AssertionError("Invalid `tags`: Must include a `description` tag for the description hash.");
        }

        boolean hasPreimageTag = this.getTags().stream().anyMatch(tag -> "preimage".equals(tag.getCode()));
        if (!hasPreimageTag) {
            throw new AssertionError("Invalid `tags`: Must include a `preimage` tag for the payment preimage.");
        }
    }
}
