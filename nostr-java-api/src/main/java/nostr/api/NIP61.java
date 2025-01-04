package nostr.api;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.NIP61Impl.NutzapEventFactory;
import nostr.api.factory.impl.NIP61Impl.NutzapInformationalEventFactory;
import nostr.base.Mint;
import nostr.base.Proof;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

public class NIP61<T extends GenericEvent> extends EventNostr<T> {

    private static final String P2PK_TAG_NAME = "pubkey";
    private static final String URL_TAG_NAME = "u";
    private static final String PROOF_TAG_NAME = "proof";

    public NIP61(@NonNull Identity sender) {
        setSender(sender);
    }

    @SuppressWarnings("unchecked")
    public NIP61<T> createNutzapInformationalEvent(@NonNull List<String> p2pkPubkey, @NonNull List<Relay> relays,
            @NonNull List<Mint> mints) {

        List<BaseTag> tags = new ArrayList<>();
        relays.forEach(relay -> tags.add(NIP42.createRelayTag(relay)));
        mints.forEach(mint -> tags.add(NIP60.createMintTag(mint)));
        p2pkPubkey.forEach(pubkey -> tags.add(NIP61.createP2pkTag(pubkey)));

        setEvent((T) new NutzapInformationalEventFactory(getSender(), tags, "").create());
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP61<T> createNutzapEvent(@NonNull NIP60.SpendingHistory.Amount amount, List<Proof> proofs,
            @NonNull Mint mint, List<EventTag> events, @NonNull PublicKey recipient, @NonNull String content) {

        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP61.createUrlTag(mint.getUrl()));
        tags.add(NIP60.createAmountTag(amount));
        tags.add(NIP60.createUnitTag(amount.getUnit()));
        tags.add(NIP01.createPubKeyTag(recipient));

        setEvent((T) new NutzapEventFactory(getSender(), tags, content).create());
        return this;
    }

    public static GenericTag createP2pkTag(@NonNull String pubkey) {
        return new TagFactory(P2PK_TAG_NAME, 61, pubkey).create();
    }

    public static GenericTag createUrlTag(@NonNull String url) {
        return new TagFactory(URL_TAG_NAME, 61, url).create();
    }

    public static GenericTag createProofTag(@NonNull Proof proof) {
        return new TagFactory(PROOF_TAG_NAME, 61, proof.toString().replace("\"", "\\\"")).create();
    }
}
