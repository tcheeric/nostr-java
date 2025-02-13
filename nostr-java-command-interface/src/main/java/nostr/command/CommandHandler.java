package nostr.command;

import nostr.context.CommandContext;

import java.io.IOException;

public interface CommandHandler {

    void handle(CommandContext context) throws IOException;
}
