package nostr.client.springwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReactiveWebSocketClient implements WebSocketClientIF {
  private final ReactorNettyWebSocketClient client;
  private final URI uri;

  public ReactiveWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.client = new ReactorNettyWebSocketClient();
    this.uri = URI.create(relayUri);
  }

  @Override
  public <T extends BaseMessage> List<String> send(T baseMessage) throws JsonProcessingException {
    return send(baseMessage.encode());
  }

  @Override
  public List<String> send(String json) {
    List<String> events = new ArrayList<>();
    client.execute(uri,
            session ->
                session
                    .send(Flux.just(session.textMessage(json)))
                    .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
                    .doOnNext(events::add).then())
        .block();
    return events;
  }

  @Override
  public void closeSocket() throws IOException {
    client.execute(uri, WebSocketSession::close).block();
  }
}
