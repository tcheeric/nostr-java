package nostr.api.client;

import nostr.api.NostrSpringWebSocketClient;
import nostr.api.integration.support.FakeWebSocketClientFactory;
import nostr.api.service.impl.DefaultNoteService;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies getRelays returns the snapshot of relay names to URIs. */
public class NostrSpringWebSocketClientRelaysTest {

  @Test
  void getRelaysReflectsRegistration() {
    Identity sender = Identity.generateRandomIdentity();
    FakeWebSocketClientFactory factory = new FakeWebSocketClientFactory();
    NostrSpringWebSocketClient client = new NostrSpringWebSocketClient(sender, new DefaultNoteService(), factory);
    client.setRelays(Map.of(
        "r1", "wss://relay1",
        "r2", "wss://relay2"));

    Map<String, String> snapshot = client.getRelays();
    assertEquals(2, snapshot.size());
    assertEquals("wss://relay1", snapshot.get("r1"));
    assertEquals("wss://relay2", snapshot.get("r2"));
  }
}
