
module nostr.connection {

    requires static lombok;

    requires okio;
    requires okhttp3;

    requires java.logging;

    requires nostr.base;
    requires nostr.event;
    requires nostr.controller;
    requires nostr.context;
    requires nostr.context.impl;
    requires org.bouncycastle.provider;

    exports nostr.connection;
    exports nostr.connection.impl;
    exports nostr.connection.impl.listeners;
}
