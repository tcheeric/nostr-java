
module nostr.connection {
    requires com.fasterxml.jackson.databind;

    requires static lombok;

    requires java.net.http;

    requires java.logging;

    requires nostr.base;
    requires nostr.command.handler;
    requires nostr.util;
    requires nostr.event;
    requires nostr.controller;
    requires nostr.context;
    requires nostr.context.impl;

    exports nostr.connection;
    exports nostr.connection.impl;
    exports nostr.connection.impl.listeners;
}
