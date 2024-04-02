package nostr.command;

import nostr.context.CommandContext;
import nostr.ws.handler.command.spi.ICommandHandler;

public interface Command {

    String getName();

    void setCommandHandler(ICommandHandler handler);

    void execute(CommandContext context);
}
