package nostr.client.springwebsocket;

import nostr.event.BaseMessage;

import java.io.IOException;

import reactor.core.publisher.Flux;

public interface WebSocketClientIF {
  <T extends BaseMessage> Flux<String> send(T eventMessage) throws IOException;

  Flux<String> send(String json) throws IOException;

  void closeSocket() throws IOException;
}
