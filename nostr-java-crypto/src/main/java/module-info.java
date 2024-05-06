
module nostr.crypto {
    requires static lombok;
    requires java.logging;

    requires org.bouncycastle.provider;

    requires nostr.util;

    exports nostr.crypto.bech32;
    exports nostr.crypto.schnorr;
    exports nostr.crypto.nip04;
    exports nostr.crypto.nip44;
}
