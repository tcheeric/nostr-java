module nostr.controller.app {
    requires nostr.context;
    requires nostr.base;
    requires nostr.event;
    requires nostr.ws;
    requires nostr.util;
    requires nostr.ws.handler;

    requires lombok;
    requires java.logging;

    requires org.eclipse.jetty.websocket.jetty.api;
    requires nostr.context.impl;
    requires nostr.controller;

    exports nostr.controller.app;
}