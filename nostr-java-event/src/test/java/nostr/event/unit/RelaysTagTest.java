package nostr.event.unit;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.tag.RelaysTag;
import org.junit.jupiter.api.Test;

class RelaysTagTest {

  public static final String RELAYS_KEY = "relays";
  public static final String HOST_VALUE = "ws://localhost:5555";
  public static final String HOST_VALUE2 = "ws://anotherlocalhost:5432";

  @Test
  void testSerialize() {
    final String expected = "[\"relays\",\"ws://localhost:5555\",\"ws://anotherlocalhost:5432\"]";
    RelaysTag relaysTag = new RelaysTag(List.of(new Relay(HOST_VALUE), new Relay(HOST_VALUE2)));
    BaseTagEncoder baseTagEncoder = new BaseTagEncoder(relaysTag);
    assertDoesNotThrow(
        () -> {
          assertEquals(expected, baseTagEncoder.encode());
        });
  }

  @Test
  void testDeserialize() {
    final String EXPECTED = "[\"relays\",\"ws://localhost:5555\"]";
    assertDoesNotThrow(
        () -> {
          JsonNode node = MAPPER_BLACKBIRD.readTree(EXPECTED);
          BaseTag deserialize = RelaysTag.deserialize(node);
          assertEquals(RELAYS_KEY, deserialize.getCode());
          assertEquals(HOST_VALUE, ((RelaysTag) deserialize).getRelays().getFirst().getUri());
        });
  }
}
