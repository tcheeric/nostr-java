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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private final WebSocketSession clientSession;
  private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

  @SneakyThrows
  public StandardWebSocketClient(@Value("${nostr.relay.uri}") String relayUri) {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    sink.tryEmitNext(message.getPayload());
  }

  @Override
  public <T extends BaseMessage> Flux<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public Flux<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    return sink.asFlux();
  }

  @Override
  public void closeSocket() throws IOException {
    clientSession.close();
    sink.tryEmitComplete();
  }
}
