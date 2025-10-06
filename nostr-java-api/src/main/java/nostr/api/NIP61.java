package nostr.api;

import java.net.URI;
import java.net.URL;
import java.util.List;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.Amount;
import nostr.event.entities.CashuMint;
import nostr.event.entities.CashuProof;
import nostr.event.entities.NutZap;
import nostr.event.entities.NutZapInformation;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

/**
 * NIP-61 helpers (Cashu Nutzap). Build informational and payment events for Cashu zaps.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/61.md">NIP-61</a>
 */
public class NIP61 extends EventNostr {

  public NIP61(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a Nutzap informational event (kind 7375) from a structured payload.
   *
   * @param nutZapInformation structured information including p2pk pubkey, relays and mints
   * @return this instance for chaining
   */
  public NIP61 createNutzapInformationalEvent(@NonNull NutZapInformation nutZapInformation) {
    return createNutzapInformationalEvent(
        List.of(nutZapInformation.getP2pkPubkey()),
        nutZapInformation.getRelays(),
        nutZapInformation.getMints());
  }

  /**
   * Create a Nutzap informational event (kind 7375).
   *
   * @param p2pkPubkey list of p2pk pubkeys supported
   * @param relays list of recommended relays
   * @param mints list of Cashu mints
   * @return this instance for chaining
   */
  public NIP61 createNutzapInformationalEvent(
      @NonNull List<String> p2pkPubkey,
      @NonNull List<Relay> relays,
      @NonNull List<CashuMint> mints) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.CASHU_NUTZAP_INFO_EVENT).create();

    relays.forEach(relay -> genericEvent.addTag(NIP42.createRelayTag(relay)));
    mints.forEach(mint -> genericEvent.addTag(NIP60.createMintTag(mint)));
    p2pkPubkey.forEach(pubkey -> genericEvent.addTag(NIP61.createP2pkTag(pubkey)));

    updateEvent(genericEvent);

    return this;
  }

  /**
   * Create a Nutzap event (kind 7374) from a structured payload.
   *
   * @param nutZap the structured Nutzap containing proofs, mint and optional target event
   * @param content optional human-readable content
   * @return this instance for chaining
   */
  @SneakyThrows
  public NIP61 createNutzapEvent(@NonNull NutZap nutZap, @NonNull String content) {

    return createNutzapEvent(
        nutZap.getProofs(),
        URI.create(nutZap.getMint().getUrl()).toURL(),
        nutZap.getNutZappedEvent(),
        nutZap.getRecipient(),
        content);
  }

  /**
   * Create a Nutzap event (kind 7374).
   *
   * @param proofs list of Cashu proofs
   * @param url the mint URL
   * @param nutzappedEventTag optional event being zapped (e-tag)
   * @param recipient the recipient public key (p-tag)
   * @param content optional human-readable content
   * @return this instance for chaining
   */
  public NIP61 createNutzapEvent(
      List<CashuProof> proofs,
      @NonNull URL url,
      EventTag nutzappedEventTag,
      @NonNull PublicKey recipient,
      @NonNull String content) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.CASHU_NUTZAP_EVENT, content).create();

    proofs.forEach(proof -> genericEvent.addTag(NIP61.createProofTag(proof)));

    if (nutzappedEventTag != null) {
      genericEvent.addTag(nutzappedEventTag);
    }
    genericEvent.addTag(NIP61.createUrlTag(url.toString()));
    genericEvent.addTag(NIP01.createPubKeyTag(recipient));

    updateEvent(genericEvent);

    return this;
  }

  @Deprecated
  public NIP61 createNutzapEvent(
      @NonNull Amount amount,
      List<CashuProof> proofs,
      @NonNull URL url,
      List<EventTag> events,
      @NonNull PublicKey recipient,
      @NonNull String content) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.CASHU_NUTZAP_EVENT, content).create();

    if (proofs != null) {
      proofs.forEach(proof -> genericEvent.addTag(NIP61.createProofTag(proof)));
    }
    if (events != null) {
      events.forEach(event -> genericEvent.addTag(event));
    }
    genericEvent.addTag(NIP61.createUrlTag(url.toString()));
    genericEvent.addTag(NIP60.createAmountTag(amount));
    genericEvent.addTag(NIP60.createUnitTag(amount.getUnit()));
    genericEvent.addTag(NIP01.createPubKeyTag(recipient));

    updateEvent(genericEvent);

    return this;
  }

  /**
   * Create a {@code p2pk} tag.
   *
   * @param pubkey the p2pk pubkey string
   * @return the created tag
   */
  public static BaseTag createP2pkTag(@NonNull String pubkey) {
    return new BaseTagFactory(Constants.Tag.P2PKH_CODE, pubkey).create();
  }

  /**
   * Create a {@code url} tag.
   *
   * @param url the URL string
   * @return the created tag
   */
  public static BaseTag createUrlTag(@NonNull String url) {
    return new BaseTagFactory(Constants.Tag.URL_CODE, url).create();
  }

  /**
   * Create a {@code proof} tag from a Cashu proof.
   *
   * @param proof the Cashu proof
   * @return the created tag
   */
  public static BaseTag createProofTag(@NonNull CashuProof proof) {
    return new BaseTagFactory(Constants.Tag.PROOF_CODE, proof.toString().replace("\"", "\\\""))
        .create();
  }
}
