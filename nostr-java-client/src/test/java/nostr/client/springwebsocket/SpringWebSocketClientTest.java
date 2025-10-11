package nostr.client.springwebsocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import nostr.event.BaseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(
    classes = {
      RetryConfig.class,
      SpringWebSocketClient.class,
      SpringWebSocketClientTest.TestConfig.class
    })
@TestPropertySource(properties = "nostr.relay.uri=wss://test")
class SpringWebSocketClientTest {

  @Configuration
  static class TestConfig {
    @Bean
    TestWebSocketClient webSocketClientIF() {
      return new TestWebSocketClient();
    }
  }

  static class TestWebSocketClient implements WebSocketClientIF {
    @Getter @Setter private int attempts;
    @Setter private int failuresBeforeSuccess;
    @Getter @Setter private int subAttempts;
    @Setter private int subFailuresBeforeSuccess;

    @Override
    public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
      return send(eventMessage.encode());
    }

    @Override
    public List<String> send(String json) throws IOException {
      attempts++;
      if (attempts <= failuresBeforeSuccess) {
        throw new IOException("fail");
      }
      return List.of("ok");
    }

    @Override
    public AutoCloseable subscribe(
        String requestJson,
        Consumer<String> messageListener,
        Consumer<Throwable> errorListener,
        Runnable closeListener)
        throws IOException {
      subAttempts++;
      if (subAttempts <= subFailuresBeforeSuccess) {
        throw new IOException("sub-fail");
      }
      return () -> {};
    }

    @Override
    public void close() {}
  }

  @Autowired private SpringWebSocketClient client;

  @Autowired private TestWebSocketClient webSocketClientIF;

  @BeforeEach
  void setup() {
    webSocketClientIF.setFailuresBeforeSuccess(0);
    webSocketClientIF.setAttempts(0);
    // Reset subscription-related state to avoid test interference across methods
    webSocketClientIF.setSubFailuresBeforeSuccess(0);
    webSocketClientIF.setSubAttempts(0);
  }

  // Ensures retryable send eventually succeeds after configured transient failures.
  @Test
  void retriesUntilSuccess() throws IOException {
    webSocketClientIF.setFailuresBeforeSuccess(2);
    List<String> result = client.send("payload");
    assertEquals(List.of("ok"), result);
    assertEquals(3, webSocketClientIF.getAttempts());
  }

  // Ensures the client surfaces the final IOException after exhausting retries.
  @Test
  void recoverAfterMaxAttempts() {
    webSocketClientIF.setFailuresBeforeSuccess(5);
    assertThrows(IOException.class, () -> client.send("payload"));
    assertEquals(3, webSocketClientIF.getAttempts());
  }

  // Ensures retryable subscribe eventually succeeds after configured transient failures.
  @Test
  void subscribeRetriesUntilSuccess() throws Exception {
    webSocketClientIF.setSubFailuresBeforeSuccess(2);
    AutoCloseable h =
        client.subscribe(
            new nostr.event.message.ReqMessage("sub-1", new nostr.event.filter.Filters[] {}),
            s -> {},
            t -> {},
            () -> {});
    h.close();
    assertEquals(3, webSocketClientIF.getSubAttempts());
  }

  // Ensures subscribe surfaces final IOException after exhausting retries.
  @Test
  void subscribeRecoverAfterMaxAttempts() {
    webSocketClientIF.setSubFailuresBeforeSuccess(5);
    assertThrows(
        IOException.class,
        () ->
            client.subscribe(
                new nostr.event.message.ReqMessage("sub-2", new nostr.event.filter.Filters[] {}),
                s -> {},
                t -> {},
                () -> {}));
    assertEquals(3, webSocketClientIF.getSubAttempts());
  }

  // Ensures retry also applies to the raw String subscribe overload.
  @Test
  void subscribeRawRetriesUntilSuccess() throws Exception {
    webSocketClientIF.setSubFailuresBeforeSuccess(1);
    AutoCloseable h =
        client.subscribe(
            "[\"REQ\",\"sub-raw\",{}]",
            s -> {},
            t -> {},
            () -> {});
    h.close();
    assertEquals(2, webSocketClientIF.getSubAttempts());
  }
}
