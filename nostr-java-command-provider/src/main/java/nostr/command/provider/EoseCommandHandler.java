package nostr.command.provider;

import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.DefaultHandler;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.context.CommandContext;

import java.util.logging.Level;

@Log
@DefaultHandler(command = Command.EOSE)
public class EoseCommandHandler extends AbstractCommandHandler {

    public EoseCommandHandler(WebSocketClientIF client) {
        super(client);
    }

    @Override
    public void handle(CommandContext context) {
        log.log(Level.INFO, "onEose event - {0}", context);
    }
}
