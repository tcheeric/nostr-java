package nostr.client.springwebsocket;

import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.BaseMessage;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

  private final WebSocketSession clientSession;
  private List<String> events = new ArrayList<>();
  private final AtomicBoolean completed = new AtomicBoolean(false);

  @SneakyThrows
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    events.add(message.getPayload());
    completed.setRelease(true);
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    await()
        .atMost(AWAIT_TIMEOUT)
        .pollInterval(POLL_INTERVAL)
        .untilTrue(completed);
    List<String> eventList = List.copyOf(events);
    events = new ArrayList<>();
    completed.setRelease(false);
    return eventList;
  }

  @Override
  public void closeSocket() throws IOException {
    clientSession.close();
  }
}
