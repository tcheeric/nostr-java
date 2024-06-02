import nostr.command.provider.AuthCommandHandler;
import nostr.command.provider.ClosedCommandHandler;
import nostr.command.provider.EoseCommandHandler;
import nostr.command.provider.EventCommandHandler;
import nostr.command.provider.NoticeCommandHandler;
import nostr.command.provider.OkCommandHandler;
import nostr.command.CommandHandler;

module nostr.command.provider {
    requires static lombok;
    requires java.logging;

    requires nostr.base;
    requires nostr.id;
    requires nostr.client;
    requires nostr.event;
    requires nostr.context;
    requires nostr.context.impl;
    requires nostr.command.handler;
  requires com.fasterxml.jackson.core;

  exports nostr.command.provider;

    provides CommandHandler with
            OkCommandHandler,
            NoticeCommandHandler,
            EoseCommandHandler,
            AuthCommandHandler,
            EventCommandHandler,
            ClosedCommandHandler;
}
