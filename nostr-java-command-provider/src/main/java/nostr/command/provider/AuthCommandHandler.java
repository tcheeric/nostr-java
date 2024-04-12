package nostr.command.provider;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.DefaultHandler;
import nostr.command.CommandHandler;
import nostr.context.CommandContext;

import java.util.logging.Level;

@DefaultHandler(command = Command.AUTH)
@NoArgsConstructor
@Log
public class AuthCommandHandler implements CommandHandler {

    @Override
    public void handle(@NonNull CommandContext context) {
        log.log(Level.INFO, "onAuth event - {0}", context);
    }
}
