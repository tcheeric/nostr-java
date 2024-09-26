package nostr.test.event;

import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.json.codec.GenericEventDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CalendarContentDecodeTest {
  String eventFullJson = """
        {
          "id": "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07",
          "kind": 30402,
          "content": "calendar content",
          "pubkey": "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984",
          "created_at": 1726114798510,
          "tags": [
            [ "d", "UUID-CalendarTimeBasedEventTest" ],
            [ "title", "calendar content title" ],
            [ "start", "1726114798510" ],
            [ "end", "1726114799510" ],
            [ "start_tzid", "America/Costa_Rica" ],
            [ "end_tzid", "America/Costa_Rica" ],
            [ "summary", "calendar summary" ],
            [ "image", "http://www.imm.org/Images/fineMotionS.jpg" ],
            [ "location", "calendar content location" ],
            [ "g", "calendar content geo-tag-1" ],
            [ "p", "444d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984" ],
            [ "p", "555d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984" ],
            [ "l", "calendar content label 1 of 2", "calendar content label 2 of 2" ],
            [ "t", "calendar content hash-tag-1111" ],
            [ "r", "http://www.imm.org/" ]
          ],
          "sig": "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"
        }
      """;

  String eventMinimalJson = """
        {
          "id": "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07",
          "kind": 30402,
          "content": "classified content",
          "pubkey": "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984",
          "created_at": 1726114798510,
          "tags": [
            [ "d", "UUID-CalendarTimeBasedEventTest" ],
            [ "title", "calendar content title" ],
            [ "start", "1726114798510" ]
          ],
          "sig": "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"
        }
      """;

  @Test
  void testCalendarContentMinimalJsonDecoding() {
    assertDoesNotThrow(() -> new GenericEventDecoder<>(CalendarTimeBasedEvent.class).decode(eventMinimalJson));
  }

  @Test
  void testCalendarContentFullJsonDecoding() {
    assertDoesNotThrow(() -> new GenericEventDecoder<>(CalendarTimeBasedEvent.class).decode(eventFullJson));
  }
}
