package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.tag.RelaysTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RelaysTagTest {
  public final static String RELAYS_KEY = "relays";
  public final static String HOST_VALUE = "ws://localhost:5555";
  public final static String HOST_VALUE2 = "ws://anotherlocalhost:5432";
  ObjectMapper mapper = new ObjectMapper();

  @Test
  void testSerialize() {
    final String expected = "[\"relays\",\"ws://localhost:5555\",\"ws://anotherlocalhost:5432\"]";
    RelaysTag relaysTag = new RelaysTag(List.of(new Relay(HOST_VALUE), new Relay(HOST_VALUE2)));
    BaseTagEncoder baseTagEncoder = new BaseTagEncoder(relaysTag);
    assertDoesNotThrow(() -> {
      assertEquals(expected, baseTagEncoder.encode());
    });
  }

  @Test
  void testDeserialize() throws JsonProcessingException {
    final String EXPECTED = "[\"relays\",\"ws://localhost:5555\"]";
    JsonNode node = new ObjectMapper().readTree(EXPECTED);

    assertDoesNotThrow(() -> {
      BaseTag deserialize = RelaysTag.deserialize(node);
      assertEquals(RELAYS_KEY, deserialize.getCode());
      assertEquals(HOST_VALUE, ((RelaysTag) deserialize).getRelays().get(0).getUri());
    });
  }
}