
module nostr.id {
    requires static lombok;
    requires nostr.base;
    requires nostr.crypto;
    requires nostr.event;
    requires nostr.util;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires org.bouncycastle.provider;
    requires java.logging;
    requires java.desktop;
    
    exports nostr.id;
}
