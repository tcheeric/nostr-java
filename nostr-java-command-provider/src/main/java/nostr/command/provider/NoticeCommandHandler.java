package nostr.command.provider;

import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.DefaultHandler;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.context.CommandContext;

import java.util.logging.Level;

@Log
@DefaultHandler(command = Command.NOTICE)
public class NoticeCommandHandler extends AbstractCommandHandler {

    public NoticeCommandHandler(WebSocketClientIF client) {
        super(client);
    }

    @Override
    public void handle(CommandContext context) {
        log.log(Level.INFO, "onNotice event - {0}", context);
    }
}
