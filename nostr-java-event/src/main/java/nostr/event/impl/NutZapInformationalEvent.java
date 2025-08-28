package nostr.event.impl;

import java.util.List;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CashuMint;
import nostr.event.entities.NutZapInformation;
import nostr.event.tag.GenericTag;

@Event(name = "Nut Zap Informational Event", nip = 61)
public class NutZapInformationalEvent extends ReplaceableEvent {

  public NutZapInformationalEvent(PublicKey pubKey, List<BaseTag> tags, String content) {
    super(pubKey, Kind.NUTZAP_INFORMATIONAL.getValue(), tags, content);
  }

  public NutZapInformation getNutZapInformation() {
    NutZapInformation nutZapInformation = new NutZapInformation();

    List<GenericTag> relayTags =
        getTags().stream()
            .filter(tag -> "relay".equals(tag.getCode()))
            .map(tag -> (GenericTag) tag)
            .toList();

    List<GenericTag> mintTags =
        getTags().stream()
            .filter(tag -> "u".equals(tag.getCode()))
            .map(tag -> (GenericTag) tag)
            .toList();

    GenericTag p2pkTag =
        getTags().stream()
            .filter(tag -> "pubkey".equals(tag.getCode()))
            .map(tag -> (GenericTag) tag)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No p2pk tag found in tags"));

    nutZapInformation.setRelays(relayTags.stream().map(this::getRelayFromTag).toList());
    nutZapInformation.setMints(mintTags.stream().map(this::getMintFromTag).toList());
    nutZapInformation.setP2pkPubkey(p2pkTag.getAttributes().get(0).value().toString());

    return nutZapInformation;
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // At least one relay
    boolean hasValidRelayTag =
        this.getTags().stream()
            .anyMatch(tag -> tag instanceof GenericTag && "relay".equals(tag.getCode()));
    if (!hasValidRelayTag) {
      throw new AssertionError("Invalid `tags`: Must include at least one valid relay tag.");
    }

    // At least one mint tag
    boolean hasValidMintTag =
        this.getTags().stream()
            .anyMatch(tag -> tag instanceof GenericTag && "u".equals(tag.getCode()));
    if (!hasValidMintTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include at least one valid mint tag with code 'u'.");
    }

    // One pubkey tag
    boolean hasValidPubKeyTag =
        this.getTags().stream()
            .anyMatch(tag -> tag instanceof GenericTag && "pubkey".equals(tag.getCode()));
    if (!hasValidPubKeyTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include exactly one pubkey tag with code 'pubkey'.");
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.NUTZAP_INFORMATIONAL.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.NUTZAP_INFORMATIONAL.getValue());
    }
  }

  private Relay getRelayFromTag(@NonNull GenericTag tag) {
    String url = tag.getAttributes().get(0).value().toString();
    return new Relay(url);
  }

  private CashuMint getMintFromTag(@NonNull GenericTag tag) {
    String mintUrl = tag.getAttributes().get(0).value().toString();
    return new CashuMint(mintUrl);
  }
}
