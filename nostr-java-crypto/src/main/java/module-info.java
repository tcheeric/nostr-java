
module nostr.crypto {
    requires java.logging;
    requires nostr.util;
    requires static lombok;
    requires org.bouncycastle.provider;
    
    exports nostr.crypto.bech32;
    exports nostr.crypto.schnorr;
}
