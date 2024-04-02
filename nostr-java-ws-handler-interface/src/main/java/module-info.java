import nostr.ws.handler.command.CommandHandler;

module nostr.ws.handler {
    requires static lombok;

    requires nostr.util;
    requires nostr.base;
    requires nostr.event;
    requires nostr.context;

    exports nostr.ws.handler.command;

    uses CommandHandler;
}
