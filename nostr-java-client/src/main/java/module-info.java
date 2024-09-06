module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.connection;
    requires nostr.context;
    requires nostr.context.impl;
    requires com.fasterxml.jackson.core;
    requires reactor.core;
    requires spring.webflux;

    exports nostr.client;
    exports nostr.client.springwebsocket;
}
