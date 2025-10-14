package nostr.api.unit;

import nostr.api.NIP42;
import nostr.base.Kind;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NIP42Test {

  @Test
  public void testCreateTags() {
    Relay relay = new Relay("wss://relay");
    BaseTag rTag = NIP42.createRelayTag(relay);
    assertEquals("relay", rTag.getCode());
    assertEquals(relay.getUri(), ((GenericTag) rTag).getAttributes().get(0).value());

    BaseTag cTag = NIP42.createChallengeTag("abc");
    assertEquals("challenge", cTag.getCode());
    assertEquals("abc", ((GenericTag) cTag).getAttributes().get(0).value());
  }

  @Test
  // Build a canonical auth event and client AUTH message; verify kind and required tags.
  public void testCanonicalAuthEventAndMessage() throws Exception {
    Identity sender = Identity.generateRandomIdentity();
    Relay relay = new Relay("wss://relay.example.com");
    NIP42 nip42 = new NIP42();
    nip42.setSender(sender);

    GenericEvent ev = nip42.createCanonicalAuthenticationEvent("token-123", relay).sign().getEvent();

    assertEquals(Kind.CLIENT_AUTH.getValue(), ev.getKind());
    assertTrue(ev.getTags().stream().anyMatch(t -> t.getCode().equals("relay")));
    assertTrue(ev.getTags().stream().anyMatch(t -> t.getCode().equals("challenge")));

    CanonicalAuthenticationEvent authEvent = GenericEvent.convert(ev, CanonicalAuthenticationEvent.class);
    assertDoesNotThrow(authEvent::validate);

    CanonicalAuthenticationMessage msg = NIP42.createClientAuthenticationMessage(authEvent);
    String json = msg.encode();
    assertTrue(json.contains("\"AUTH\""));
    // Encoded AUTH message should embed the full event JSON including tags
    assertTrue(json.contains("\"tags\""));
    assertTrue(json.contains("relay"));
    assertTrue(json.contains("challenge"));
  }

  @Test
  // Relay AUTH message includes challenge string.
  public void testRelayAuthMessage() throws Exception {
    String json = NIP42.createRelayAuthenticationMessage("c-1").encode();
    assertTrue(json.contains("\"AUTH\""));
    assertTrue(json.contains("\"c-1\""));
  }
}
