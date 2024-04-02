package nostr.reqsp.impl;

import lombok.Data;
import nostr.reqsp.Response;
import nostr.ws.handler.command.spi.CommandHandler;

@Data
public class DefaultResponse implements Response {

    private CommandHandler commandHandler;
}
