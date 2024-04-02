module nostr.command.impl {

    requires nostr.command;
    requires nostr.context;
    requires nostr.context.impl;
    requires nostr.ws.handler;
    requires nostr.base;

    requires lombok;

    requires java.logging;

    exports nostr.command.impl;
}