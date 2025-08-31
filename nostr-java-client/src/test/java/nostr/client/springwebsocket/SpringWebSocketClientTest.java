package nostr.client.springwebsocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
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
    public void close() {}
  }

  @Autowired private SpringWebSocketClient client;

  @Autowired private TestWebSocketClient webSocketClientIF;

  @BeforeEach
  void setup() {
    webSocketClientIF.setFailuresBeforeSuccess(0);
    webSocketClientIF.setAttempts(0);
  }

  @Test
  void retriesUntilSuccess() throws IOException {
    webSocketClientIF.setFailuresBeforeSuccess(2);
    List<String> result = client.send("payload");
    assertEquals(List.of("ok"), result);
    assertEquals(3, webSocketClientIF.getAttempts());
  }

  @Test
  void recoverAfterMaxAttempts() {
    webSocketClientIF.setFailuresBeforeSuccess(5);
    assertThrows(IOException.class, () -> client.send("payload"));
    assertEquals(3, webSocketClientIF.getAttempts());
  }
}
