package nostr.examples;

import nostr.base.ElementAttribute;
import nostr.base.Kind;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

import java.time.Instant;
import java.util.List;

/**
 * Example demonstrating creation of an expiration event (NIP-40) and showing how to send it with
 * either available WebSocket client.
 */
public class ExpirationEventExample {

  private static final String RELAY_URI = "ws://localhost:5555";
  private static final long EXPIRATION_SECONDS = 3600; // 1 hour

  private static GenericEvent createExpirationEvent() {
    Identity identity = Identity.generateRandomIdentity();
    long expiration = Instant.now().plusSeconds(EXPIRATION_SECONDS).getEpochSecond();
    BaseTag expirationTag =
        new GenericTag("expiration", new ElementAttribute("param0", String.valueOf(expiration)));
    GenericEvent event =
        new GenericEvent(
            identity.getPublicKey(),
            Kind.TEXT_NOTE,
            List.of(expirationTag),
            "This message will expire at the specified timestamp and be deleted by relays.\n");
    identity.sign(event);
    return event;
  }

  private static void sendWithStandardClient(GenericEvent event) throws Exception {
    try (StandardWebSocketClient client = new StandardWebSocketClient(RELAY_URI)) {
      client.send(new EventMessage(event));
    }
  }

  private static void sendWithSpringClient(GenericEvent event) throws Exception {
    try (SpringWebSocketClient client =
        new SpringWebSocketClient(new StandardWebSocketClient(RELAY_URI), RELAY_URI)) {
      client.send(new EventMessage(event));
    }
  }

  public static void main(String[] args) throws Exception {
    GenericEvent event = createExpirationEvent();
    // Alternative: use the standard client instead of the Spring client. It waits for
    // a relay response and does not automatically retry failed sends.
    // sendWithStandardClient(event);
    sendWithSpringClient(event);
  }
}
