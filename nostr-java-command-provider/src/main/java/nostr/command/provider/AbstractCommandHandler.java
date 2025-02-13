package nostr.command.provider;

import lombok.Getter;
import nostr.client.springwebsocket.WebSocketClientIF;
import nostr.command.CommandHandler;

@Getter
public abstract class AbstractCommandHandler implements CommandHandler {
  private final WebSocketClientIF client;

  public AbstractCommandHandler(WebSocketClientIF client) {
    this.client = client;
  }
}
