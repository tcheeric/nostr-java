package nostr.command.provider;

import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.DefaultHandler;
import nostr.command.CommandHandler;
import nostr.context.CommandContext;

import java.util.logging.Level;

@Log
@DefaultHandler(command = Command.OK)
@NoArgsConstructor
public class OkCommandHandler implements CommandHandler {

    @Override
    public void handle(CommandContext context) {
        log.log(Level.INFO, "onOk event - {0}", context);
    }
}
