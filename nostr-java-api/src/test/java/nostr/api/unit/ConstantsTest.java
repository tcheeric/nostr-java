package nostr.api.unit;

import static nostr.base.json.EventJsonMapper.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class ConstantsTest {

  @Test
  void testKindValues() {
    assertEquals(0, Constants.Kind.USER_METADATA);
    assertEquals(1, Constants.Kind.SHORT_TEXT_NOTE);
    assertEquals(42, Constants.Kind.CHANNEL_MESSAGE);
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
    event.setKind(Constants.Kind.SHORT_TEXT_NOTE);
    event.setPubKey(identity.getPublicKey());
    event.setCreatedAt(0L);
    event.setContent("test");

    String json = new BaseEventEncoder<>(event).encode();
    assertEquals(
        Constants.Kind.SHORT_TEXT_NOTE, mapper().readTree(json).get("kind").asInt());
  }
}
