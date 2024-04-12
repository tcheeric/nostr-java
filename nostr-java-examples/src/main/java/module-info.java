
module nostr.examples {
    requires nostr.event;
    requires nostr.client;
    requires nostr.api;
    requires static lombok;
    requires nostr.connection;
    requires nostr.util;
    requires nostr.base;
    requires nostr.id;
    requires org.bouncycastle.provider;
    requires java.logging;
    requires nostr.context.impl;

    exports nostr.examples;
}
