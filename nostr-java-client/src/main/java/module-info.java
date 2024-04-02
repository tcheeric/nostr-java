module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.context;
    requires nostr.context.impl;
    requires nostr.controller;
    requires nostr.id;
    requires nostr.ws;
    requires nostr.controller.app;
    requires nostr.ws.handler;
    requires nostr.ws.request.handler.provider;

    requires org.bouncycastle.provider;

    exports nostr.client;
}
