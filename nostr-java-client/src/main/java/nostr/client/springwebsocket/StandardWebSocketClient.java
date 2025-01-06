package nostr.client.springwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import nostr.event.BaseMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private final WebSocketSession clientSession;
  private List<String> events = new ArrayList<>();
  private boolean completed = false;

  @SneakyThrows
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
  }

  @Override
  protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
    events.add(message.getPayload());
    completed = true;
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws JsonProcessingException, IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    await().until(() -> completed);
    List<String> eventList = List.copyOf(events);
    events = new ArrayList<>();
    completed = false;
    return eventList;
  }

  @Override
  public void closeSocket() throws IOException {
    clientSession.close();
  }
}
