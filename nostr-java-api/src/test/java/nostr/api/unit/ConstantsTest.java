package nostr.api.unit;

import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.base.Kind;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class ConstantsTest {

  @Test
  void testKindValues() {
    // Validate a few representative Kind enum values remain stable
    assertEquals(0, Kind.SET_METADATA.getValue());
    assertEquals(1, Kind.TEXT_NOTE.getValue());
    assertEquals(42, Kind.CHANNEL_MESSAGE.getValue());
  }

  @Test
  void testTagValues() {
    assertEquals("e", Constants.Tag.EVENT_CODE);
    assertEquals("p", Constants.Tag.PUBKEY_CODE);
  }

  @Test
  void testSerializationWithConstants() throws Exception {
    Identity identity = Identity.generateRandomIdentity();
    GenericEvent event = new GenericEvent();
    event.setKind(Kind.TEXT_NOTE.getValue());
    event.setPubKey(identity.getPublicKey());
    event.setCreatedAt(0L);
    event.setContent("test");

    String json = new BaseEventEncoder<>(event).encode();
    assertEquals(Kind.TEXT_NOTE.getValue(), mapper().readTree(json).get("kind").asInt());
  }
}
