package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.Amount;
import nostr.event.entities.CashuMint;
import nostr.event.entities.CashuProof;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

import java.net.URL;
import java.util.List;

public class NIP61 extends EventNostr {

    public NIP61(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP61 createNutzapInformationalEvent
            (@NonNull List<String> p2pkPubkey,
             @NonNull List<Relay> relays,
             @NonNull List<CashuMint> mints) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_NUTZAP_INFO_EVENT).create();

        relays.forEach(relay -> genericEvent.addTag(NIP42.createRelayTag(relay)));
        mints.forEach(mint -> genericEvent.addTag(NIP60.createMintTag(mint)));
        p2pkPubkey.forEach(pubkey -> genericEvent.addTag(NIP61.createP2pkTag(pubkey)));

        updateEvent(genericEvent);

        return this;
    }

    public NIP61 createNutzapEvent(
            @NonNull Amount amount,
            List<CashuProof> proofs,
            @NonNull URL url,
            List<EventTag> events,
            @NonNull PublicKey recipient,
            @NonNull String content) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_NUTZAP_EVENT, content).create();

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

    public static BaseTag createP2pkTag(@NonNull String pubkey) {
        return new BaseTagFactory(Constants.Tag.P2PKH_CODE, pubkey).create();
    }

    public static BaseTag createUrlTag(@NonNull String url) {
        return new BaseTagFactory(Constants.Tag.URL_CODE, url).create();
    }

    public static BaseTag createProofTag(@NonNull CashuProof proof) {
        return new BaseTagFactory(Constants.Tag.PROOF_CODE, proof.toString().replace("\"", "\\\"")).create();
    }
}
