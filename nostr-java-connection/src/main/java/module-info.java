
module nostr.connection {

    requires static lombok;

    requires java.net.http;

    requires java.logging;

    requires nostr.base;
    requires nostr.event;
    requires nostr.controller;
    requires nostr.context;
    requires nostr.context.impl;

    exports nostr.connection;
    exports nostr.connection.impl;
    exports nostr.connection.impl.listeners;
}
