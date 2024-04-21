module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.connection;
    requires nostr.context;
    requires nostr.context.impl;
    requires nostr.controller;
    requires nostr.id;

    requires org.bouncycastle.provider;

    exports nostr.client;
}
