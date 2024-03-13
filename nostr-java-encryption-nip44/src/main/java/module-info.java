module nostr.encryption.nip44dm {

    requires lombok;
    requires java.logging;

    requires nostr.encryption;
    requires nostr.crypto;
    requires nostr.util;
    requires org.bouncycastle.provider;

    exports nostr.encryption.nip44;
}