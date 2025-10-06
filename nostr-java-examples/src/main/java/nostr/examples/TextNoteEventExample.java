package nostr.examples;

import java.util.List;
import nostr.client.springwebsocket.StandardWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.TextNoteEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;

/**
 * Demonstrates creating, signing, and sending a text note using the
 * {@link nostr.event.impl.TextNoteEvent} class.
 */
public class TextNoteEventExample {

  private static final String RELAY_URI = "ws://localhost:5555";

  public static void main(String[] args) throws Exception {
    Identity identity = Identity.generateRandomIdentity();
    TextNoteEvent event =
        new TextNoteEvent(
            identity.getPublicKey(), List.<BaseTag>of(), "Hello from TextNoteEvent!\n");
    identity.sign(event);
    try (StandardWebSocketClient client = new StandardWebSocketClient(RELAY_URI)) {
      client.send(new EventMessage(event));
    }
    System.out.println(event);
  }
}
