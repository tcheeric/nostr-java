
module nostr.crypto {
    requires nostr.util;
    requires static lombok;
    requires java.logging;
    requires org.bouncycastle.provider;
    
    exports nostr.crypto.bech32;
    exports nostr.crypto.schnorr;
    exports nostr.crypto.nip04;
    exports nostr.crypto.nip44;
}
