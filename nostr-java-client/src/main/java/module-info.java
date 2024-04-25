module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.connection;
    requires nostr.context;
    requires nostr.context.impl;

    exports nostr.client;
}
