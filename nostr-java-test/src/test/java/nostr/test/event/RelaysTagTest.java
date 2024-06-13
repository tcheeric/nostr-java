package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.event.BaseTag;
import nostr.event.tag.RelaysTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RelaysTagTest {

  public final static String RELAYS_KEY = "relays";
  public final static String HOST_VALUE = "ws://localhost:5555";
  @Test
  void testDeserializer() throws JsonProcessingException {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree("[\"relays\",\"ws://localhost:5555\"]");

    assertDoesNotThrow(() -> {
      BaseTag deserialize = RelaysTag.deserialize(node);
      assertEquals(RELAYS_KEY, deserialize.getCode());
      assertEquals(HOST_VALUE, ((RelaysTag) deserialize).getRelays().get(0).getUri());
    });
  }
}