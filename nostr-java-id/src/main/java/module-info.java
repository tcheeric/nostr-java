
module nostr.id {
    requires static lombok;
    requires nostr.base;
    requires nostr.crypto;
    requires nostr.event;
    requires nostr.util;
    requires org.bouncycastle.provider;
    requires java.logging;
    requires java.desktop;
    
    exports nostr.id;
}
