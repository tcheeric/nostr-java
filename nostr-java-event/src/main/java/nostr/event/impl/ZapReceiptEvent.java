
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.Kind;
import nostr.event.json.serializer.ZapReceiptSerializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

import java.util.Optional;

@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapReceiptEvent", nip = 57)
public class ZapReceiptEvent extends GenericEvent {
  private final ZapReceipt zapReceipt;

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull PubKeyTag zapRequestPubKeyTag, EventTag zapRequestEventTag,
      AddressTag zapRequestAddressTag, @NonNull ZapReceipt zapReceipt) {
    super(pubKey, Kind.ZAP_RECEIPT);
    super.addTag(zapRequestPubKeyTag);
    Optional.ofNullable(zapRequestEventTag).ifPresent(super::addTag);
    Optional.ofNullable(zapRequestAddressTag).ifPresent(super::addTag);
    this.zapReceipt = zapReceipt;
  }

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull String zapRequestPubKeyTag, String zapRequestEventTag,
      AddressTag zapRequestAddressTag, @NonNull ZapReceipt zapReceipt) {
    this(pubKey, new PubKeyTag(new PublicKey(zapRequestPubKeyTag)), new EventTag(zapRequestEventTag), zapRequestAddressTag, zapReceipt);
  }

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull String zapRequestPubKeyTag, String zapRequestEventTag,
      String zapRequestAddressTag, String zapRequestIdentifier, String zapRequestRelayUri, @NonNull String bolt11,
      @NonNull String descriptionSha256, @NonNull String preimage) {
    this(pubKey, zapRequestPubKeyTag, zapRequestEventTag,
        new AddressTag(null,
            new PublicKey(zapRequestAddressTag),
            new IdentifierTag(zapRequestIdentifier),
            new Relay(zapRequestRelayUri)),
        new ZapReceipt(bolt11, descriptionSha256, preimage));
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  @JsonSerialize(using = ZapReceiptSerializer.class)
  public static class ZapReceipt extends AbstractEventContent<ZapReceiptEvent> {
    @JsonProperty
    private String id;

    @JsonProperty
    private String bolt11;

    @JsonProperty
    private String descriptionSha256; // must match bolt11 invoice hash

    @JsonProperty
    private String preimage;

    public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256, String preimage) {
      this.descriptionSha256 = descriptionSha256; //      TODO: check hash match
      this.bolt11 = bolt11;
      this.preimage = preimage;
    }

    public ZapReceipt(@NonNull String bolt11, @NonNull String descriptionSha256) {
      this(bolt11, descriptionSha256, null);
    }
  }
}
