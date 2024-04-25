package nostr.command;

import nostr.context.CommandContext;

public interface CommandHandler {

    void handle(CommandContext context);
}
