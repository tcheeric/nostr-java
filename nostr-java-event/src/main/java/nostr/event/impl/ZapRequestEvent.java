
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

    public ZapRequestEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(recipientPubKey, Kind.ZAP_REQUEST, tags, content);
    }

    public ZapRequest getZapRequest() {
        BaseTag relaysTag = getTag("relays");
        BaseTag amountTag = getTag("amount");
        BaseTag lnUrlTag = getTag("lnurl");

        return new ZapRequest(
                (RelaysTag) relaysTag,
                Long.parseLong(((GenericTag) amountTag).getAttributes().get(0).value().toString()),
                ((GenericTag) lnUrlTag).getAttributes().get(0).value().toString()
        );
    }

    public PublicKey getRecipientKey() {
        return this.getTags().stream()
                .filter(tag -> "p".equals(tag.getCode()))
                .map(tag -> ((PubKeyTag) tag).getPublicKey())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Recipient public key not found in tags"));
    }

    public String getEventId() {
        return this.getTags().stream()
                .filter(tag -> "e".equals(tag.getCode()))
                .map(tag -> ((GenericTag) tag).getAttributes().get(0).value().toString())
                .findFirst()
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
        boolean hasRecipientTag = this.getTags().stream().anyMatch(tag -> "p".equals(tag.getCode()));
        if (!hasRecipientTag) {
            throw new AssertionError("Invalid `tags`: Must include a `p` tag for the recipient's public key.");
        }

        boolean hasAmountTag = this.getTags().stream().anyMatch(tag -> "amount".equals(tag.getCode()));
        if (!hasAmountTag) {
            throw new AssertionError("Invalid `tags`: Must include an `amount` tag specifying the amount in millisatoshis.");
        }

        boolean hasLnUrlTag = this.getTags().stream().anyMatch(tag -> "lnurl".equals(tag.getCode()));
        if (!hasLnUrlTag) {
            throw new AssertionError("Invalid `tags`: Must include an `lnurl` tag containing the Lightning Network URL.");
        }
    }

    @Override
    protected void validateKind() {
        if (getKind() != Kind.ZAP_REQUEST.getValue()) {
            throw new AssertionError("Invalid kind value. Expected " + Kind.ZAP_REQUEST.getValue());
        }
    }
}
