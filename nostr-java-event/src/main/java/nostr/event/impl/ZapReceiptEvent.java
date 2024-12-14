
package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.tag.*;

import java.util.Optional;

@Getter
@EqualsAndHashCode(callSuper = false)
@Event(name = "ZapReceiptEvent", nip = 57)
public class ZapReceiptEvent extends GenericEvent {
  @JsonIgnore
  private final ZapReceipt zapReceipt;

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull PubKeyTag zapRequestPubKeyTag, EventTag zapRequestEventTag,
      AddressTag zapRequestAddressTag, @NonNull ZapReceipt zapReceipt) {
    super(pubKey, Kind.ZAP_RECEIPT);
    super.addTag(zapRequestPubKeyTag);
    Optional.ofNullable(zapRequestEventTag).ifPresent(super::addTag);
    Optional.ofNullable(zapRequestAddressTag).ifPresent(super::addTag);
    this.zapReceipt = zapReceipt;
  }

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull String zapRequestPubKeyTag, String zapRequestEventTag, AddressTag zapRequestAddressTag, @NonNull ZapReceipt zapReceipt) {
    this(pubKey, new PubKeyTag(new PublicKey(zapRequestPubKeyTag)), new EventTag(zapRequestEventTag), zapRequestAddressTag, zapReceipt);
  }

  public ZapReceiptEvent(@NonNull PublicKey pubKey, @NonNull String zapRequestPubKeyTag, String zapRequestEventTag, String zapRequestAddressTag, String zapRequestIdentifier, String zapRequestRelayUri, @NonNull String bolt11, @NonNull String descriptionSha256, @NonNull String preimage) {
    this(pubKey, zapRequestPubKeyTag, zapRequestEventTag,
        new AddressTag(null,
            new PublicKey(zapRequestAddressTag),
            new IdentifierTag(zapRequestIdentifier),
            new Relay(zapRequestRelayUri)),
        new ZapReceipt(bolt11, descriptionSha256, preimage));
  }

  @Override
  protected void validate() {
    if (getKind() == 9735)
      return;

    throw new AssertionError(String.format("Invalid kind value [%s]. Zap Receipt must be of kind 9735", getKind()), null);
  }
}
