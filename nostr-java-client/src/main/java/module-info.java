module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.context;
    requires com.fasterxml.jackson.core;
    requires reactor.core;
    requires spring.webflux;
    requires spring.context;
    requires spring.beans;
    requires spring.websocket;
    requires jakarta.websocket.client;
    requires awaitility;

    exports nostr.client.springwebsocket;
}
