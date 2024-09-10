module nostr.api {
    requires nostr.base;
    requires nostr.util;
    requires nostr.event;
    requires nostr.id;
    requires nostr.client;
    requires nostr.context;
    requires nostr.context.impl;
    requires nostr.encryption;
    requires nostr.encryption.nip04dm;
    requires nostr.encryption.nip44dm;
    
    requires com.fasterxml.jackson.databind;
    
    requires lombok;
    requires java.logging;
    requires nostr.crypto;
    requires spring.websocket;
    requires reactor.core;
    requires org.reactivestreams;
    requires annotations;

    exports nostr.api;
}
