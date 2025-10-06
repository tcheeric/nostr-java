package nostr.event.impl;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CashuMint;
import nostr.event.entities.CashuProof;
import nostr.event.entities.NutZap;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;

@EqualsAndHashCode(callSuper = true)
@Event(name = "Nut Zap Event", nip = 61)
@Data
public class NutZapEvent extends GenericEvent {

  public NutZapEvent(PublicKey sender, List<BaseTag> tags, String content) {
    super(sender, Kind.NUTZAP, tags, content);
  }

  public NutZap getNutZap() {

    NutZap nutZap = new NutZap();

    EventTag zappedEvent =
        nostr.event.filter.Filterable.getTypeSpecificTags(EventTag.class, this).stream()
            .findFirst()
            .orElse(null);

    List<GenericTag> proofs =
        nostr.event.filter.Filterable.getTypeSpecificTags(GenericTag.class, this).stream()
            .filter(tag -> "proof".equals(tag.getCode()))
            .toList();

    PubKeyTag recipientTag =
        nostr.event.filter.Filterable.getTypeSpecificTags(PubKeyTag.class, this).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No PubKeyTag found in tags"));

    GenericTag mintTag =
        nostr.event.filter.Filterable.getTypeSpecificTags(GenericTag.class, this).stream()
            .filter(tag -> "u".equals(tag.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No mint tag found in tags"));

    nutZap.setMint(getMintFromTag(mintTag));
    proofs.forEach(
        proofTag -> {
          CashuProof cashuProof = getProofFromTag(proofTag);
          nutZap.addProof(cashuProof);
        });
    nutZap.setRecipient(recipientTag.getPublicKey());
    nutZap.setNutZappedEvent(zappedEvent);

    return nutZap;
  }

  protected void validateTags() {
    super.validateTags();

    // Validate `tags` field for the mint tag
    boolean hasMintTag = this.getTags().stream().anyMatch(tag -> "u".equals(tag.getCode()));
    if (!hasMintTag) {
      throw new AssertionError("Invalid `tags`: Must include a mint tag with code 'u'.");
    }

    // Validate `tags` field for at exactly one PubKeyTag
    boolean hasValidPubKeyTag = this.getTags().stream().anyMatch(tag -> tag instanceof PubKeyTag);
    if (!hasValidPubKeyTag) {
      throw new AssertionError("Invalid `tags`: Must include one valid PubKeyTag.");
    }

    // Validate `tags` field for at least one Proof tag
    boolean hasProofTag = this.getTags().stream().anyMatch(tag -> "proof".equals(tag.getCode()));
    if (!hasProofTag) {
      throw new AssertionError(
          "Invalid `tags`: Must include at least one Proof tag with code 'proof'.");
    }
  }

  @Override
  protected void validateKind() {
    if (getKind() != Kind.NUTZAP.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.NUTZAP.getValue());
    }
  }

  private CashuMint getMintFromTag(GenericTag mintTag) {
    String url = mintTag.getAttributes().get(0).value().toString();
    CashuMint mint = new CashuMint(url);
    return mint;
  }

  private CashuProof getProofFromTag(GenericTag proofTag) {
    String proof = proofTag.getAttributes().get(0).value().toString();
    CashuProof cashuProof = IEvent.MAPPER_BLACKBIRD.convertValue(proof, CashuProof.class);
    return cashuProof;
  }
}
