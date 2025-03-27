package nostr.event.unit;

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
            [ "p", "444d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984", "ws://localhost:5555", "PAYER" ],
            [ "p", "555d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984", "ws://localhost:5555", "PAYEE" ],
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

  String problematicBarchettaJson = """
      {
        "id": "a21f990312c06e063af233935a1b7021e2824cedd0c5a46e160acb182e07637c",
        "kind": 31923,
        "content": "CALENDAR-EVENT CONTENT",
        "pubkey": "111df01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f",
        "created_at": 1727482684,
        "tags": [
          [
            "d",
            "UUID-NEEDS-COMPLETION-001"
          ],
          [
            "end",
            "1727482683878"
          ],
          [
            "title",
            "1111111"
          ],
          [
            "start",
            "1727482683878"
          ]
        ],
        "sig": "c326e782307d740416bf5cb8c9635f7d1b93dec75e61b1ad8d7214a4b61d724230c9f3adb71dfc054b1f77c1ad3b73a2a7802205c64928cf0bbbcfbaf60e8552"
      }
      """;

  @Test
  void testCalendarContentMinimalJsonDecoding() {
    assertDoesNotThrow(() ->
      new GenericEventDecoder<>(CalendarTimeBasedEvent.class).decode(eventMinimalJson));
  }

  @Test
  void testCalendarContentFullJsonDecoding() {
    assertDoesNotThrow(() ->
      new GenericEventDecoder<>(CalendarTimeBasedEvent.class).decode(eventFullJson));
  }

  @Test
  void testCalendarContentProblemBarchettaJsonDecoding() {
    assertDoesNotThrow(() ->
      new GenericEventDecoder<>(CalendarTimeBasedEvent.class).decode(eventFullJson));
  }
}
