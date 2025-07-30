
package nostr.event.impl;

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

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapReceiptEvent", nip = 57)
@NoArgsConstructor
public class ZapReceiptEvent extends GenericEvent {

    public ZapReceiptEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(recipientPubKey, Kind.ZAP_RECEIPT, tags, content);
    }

    public ZapReceipt getZapReceipt() {
        BaseTag preimageTag = requireTag("preimage");
        BaseTag descriptionTag = requireTag("description");
        BaseTag bolt11Tag = requireTag("bolt11");

        return new ZapReceipt(
                ((GenericTag) bolt11Tag).getAttributes().get(0).getValue().toString(),
                ((GenericTag) descriptionTag).getAttributes().get(0).getValue().toString(),
                ((GenericTag) preimageTag).getAttributes().get(0).getValue().toString()
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
        PubKeyTag recipientPubKeyTag = (PubKeyTag) requireTag("p");
        return recipientPubKeyTag.getPublicKey();
    }

    public PublicKey getSender() {
        BaseTag senderTag = getTag("P");
        if (senderTag == null) {
            return null;
        }
        PubKeyTag senderPubKeyTag = (PubKeyTag) senderTag;
        return senderPubKeyTag.getPublicKey();
    }

    public String getEventId() {
        BaseTag eventTag = getTag("e");
        if (eventTag == null) {
            return null;
        }
        return ((GenericTag) eventTag).getAttributes().get(0).getValue().toString();
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate `tags` field
        // Check for required tags
        requireTag("p");
        requireTag("bolt11");
        requireTag("description");
        requireTag("preimage");
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.ZAP_RECEIPT.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.ZAP_RECEIPT.getValue());
        }
    }
}
