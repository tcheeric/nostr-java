package nostr.client.springwebsocket;

import lombok.NonNull;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Optional;

public class WebSocketHandler {
  private final Sinks.Many<String> sendBuffer;
  private final Sinks.Many<String> receiveBuffer;
  private Disposable subscription;
  private WebSocketSession session;

  public WebSocketHandler() {
//    TODO: revisit, possibly other options/approaches
    this.sendBuffer = Sinks.many().unicast().onBackpressureBuffer();
    this.receiveBuffer = Sinks.many().unicast().onBackpressureBuffer();
  }

  protected void connect(@NonNull WebSocketClient webSocketClient, @NonNull URI uri) {
    subscription =
        webSocketClient
            .execute(uri, this::handleSession)
            .then(Mono.fromRunnable(this::onClose))
            .subscribe();
  }

  protected void disconnect() {
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
      subscription = null;
      onClose();
    }
  }

  protected void send(@NonNull String message) {
    sendBuffer.tryEmitNext(message);
  }

  protected Flux<String> receive() {
    return receiveBuffer.asFlux();
  }

  protected Optional<WebSocketSession> session() {
    return Optional.ofNullable(session);
  }

  private Mono<Void> handleSession(WebSocketSession session) {
    onOpen(session);

    Mono<Void> input =
        session
            .receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(receiveBuffer::tryEmitNext)
            .then();

    Mono<Void> output =
        session
            .send(
                sendBuffer
                    .asFlux()
                    .map(session::textMessage)
            );

    return
        Mono
            .zip(input, output)
            .then();
  }

  private void onOpen(WebSocketSession session) {
    this.session = session;
  }

  private void onClose() {
    session = null;
  }
}
