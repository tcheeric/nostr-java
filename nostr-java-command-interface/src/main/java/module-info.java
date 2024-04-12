import nostr.command.CommandHandler;

module nostr.command.handler {
    requires static lombok;

    requires nostr.util;
    requires nostr.base;
    requires nostr.event;
    requires nostr.context;

    exports nostr.command;

    uses CommandHandler;
}
