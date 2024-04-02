package nostr.ws.handler.command.provider;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.DefaultHandler;
import nostr.context.CommandContext;
import nostr.ws.handler.command.CommandHandler;

import java.util.logging.Level;

@Log
@DefaultHandler(command = Command.EOSE)
@NoArgsConstructor
public class EoseCommandHandler implements CommandHandler {

    @Override
    public void handle(CommandContext context) {
        log.log(Level.INFO, "onEose event - {0}", context);
    }
}
