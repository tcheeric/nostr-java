package nostr.examples;

import nostr.api.NIP01;
import nostr.id.Identity;

import java.util.Map;

/**
 * Example showing how to create, sign and send a text note using the NIP01 helper built on top of
 * NostrSpringWebSocketClient.
 */
public class SpringClientTextEventExample {

  private static final Map<String, String> RELAYS = Map.of("local", "ws://localhost:5555");

  public static void main(String[] args) {
    Identity sender = Identity.generateRandomIdentity();
    NIP01 client = new NIP01(sender);
    client.setRelays(RELAYS);
    client.createTextNoteEvent("Hello from NostrSpringWebSocketClient!\n").signAndSend();
  }
}
