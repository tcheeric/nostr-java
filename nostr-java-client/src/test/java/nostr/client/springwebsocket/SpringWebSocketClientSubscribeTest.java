package nostr.client.springwebsocket;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(
    classes = {
      RetryConfig.class,
      SpringWebSocketClient.class,
      SpringWebSocketClientSubscribeTest.TestConfig.class
    })
@TestPropertySource(properties = "nostr.relay.uri=wss://test")
class SpringWebSocketClientSubscribeTest {

  @Configuration
  static class TestConfig {
    @Bean
    EmitterWebSocketClient webSocketClientIF() {
      return new EmitterWebSocketClient();
    }
  }

  static class EmitterWebSocketClient implements WebSocketClientIF {
    @Getter private String lastJson;
    private Consumer<String> messageListener;
    private Consumer<Throwable> errorListener;
    private Runnable closeListener;

    @Override
    public <T extends nostr.event.BaseMessage> java.util.List<String> send(T eventMessage)
        throws IOException {
      return send(eventMessage.encode());
    }

    @Override
    public java.util.List<String> send(String json) throws IOException {
      lastJson = json;
      return java.util.List.of();
    }

    @Override
    public AutoCloseable subscribe(
        String requestJson,
        Consumer<String> messageListener,
        Consumer<Throwable> errorListener,
        Runnable closeListener)
        throws IOException {
      this.lastJson = requestJson;
      this.messageListener = messageListener;
      this.errorListener = errorListener;
      this.closeListener = closeListener;
      return () -> {
        if (this.closeListener != null) this.closeListener.run();
      };
    }

    @Override
    public void close() {}

    void emit(String payload) { if (messageListener != null) messageListener.accept(payload); }
    void emitError(Throwable t) { if (errorListener != null) errorListener.accept(t); }
  }

  @Autowired private SpringWebSocketClient client;
  @Autowired private EmitterWebSocketClient webSocketClientIF;

  @Test
  void subscribeReceivesMessagesAndErrorAndClose() throws Exception {
    AtomicInteger messages = new AtomicInteger();
    AtomicInteger errors = new AtomicInteger();
    AtomicInteger closes = new AtomicInteger();

    AutoCloseable handle =
        client.subscribe(
            new nostr.event.message.ReqMessage("sub-1", new nostr.event.filter.Filters[] {}),
            payload -> messages.incrementAndGet(),
            t -> errors.incrementAndGet(),
            closes::incrementAndGet
        );

    webSocketClientIF.emit("EVENT");
    webSocketClientIF.emitError(new IOException("boom"));
    handle.close();

    assertEquals(1, messages.get());
    assertEquals(1, errors.get());
    assertEquals(1, closes.get());
    assertTrue(webSocketClientIF.getLastJson().contains("\"REQ\""));
  }
}
