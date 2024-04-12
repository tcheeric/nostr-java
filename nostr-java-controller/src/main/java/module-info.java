import nostr.command.CommandHandler;

module nostr.controller {
    uses CommandHandler;

    requires static lombok;

    requires java.logging;

    requires nostr.context;
    requires nostr.event;
    requires nostr.context.impl;
    requires nostr.base;
    requires nostr.util;
    requires nostr.command.handler;

    exports nostr.controller.impl;
    exports nostr.controller;
}