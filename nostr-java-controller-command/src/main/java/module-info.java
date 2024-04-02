module nostr.controller.command {

    requires static lombok;

    requires java.logging;

    requires nostr.controller;
    requires nostr.context;
    requires nostr.event;
    requires nostr.context.impl;
    requires nostr.base;
    requires nostr.ws.handler;

    exports nostr.controller.command;
}