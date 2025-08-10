package nostr.client.springwebsocket;

import nostr.event.BaseMessage;

import java.io.IOException;
import java.util.List;

public interface WebSocketClientIF extends AutoCloseable {
  <T extends BaseMessage> List<String> send(T eventMessage) throws IOException;
  List<String> send(String json) throws IOException;

  @Override
  void close() throws IOException;
}
