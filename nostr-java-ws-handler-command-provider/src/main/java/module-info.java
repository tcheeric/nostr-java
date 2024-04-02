import nostr.ws.handler.command.provider.AuthCommandHandler;
import nostr.ws.handler.command.provider.EoseCommandHandler;
import nostr.ws.handler.command.provider.EventCommandHandler;
import nostr.ws.handler.command.provider.NoticeCommandHandler;
import nostr.ws.handler.command.provider.OkCommandHandler;
import nostr.ws.handler.command.CommandHandler;

module nostr.ws.handler.command.provider {
    requires static lombok;
    requires java.logging;

    requires nostr.ws.handler;
    requires nostr.util;
    requires nostr.base;
    requires nostr.id;
    requires nostr.client;
    requires nostr.event;
    requires nostr.context;
    requires nostr.context.impl;

    exports nostr.ws.handler.command.provider;

    provides CommandHandler with OkCommandHandler, NoticeCommandHandler, EoseCommandHandler, AuthCommandHandler, EventCommandHandler;
}
