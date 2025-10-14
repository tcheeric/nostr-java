package nostr.event.unit;

import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.json.codec.GenericEventDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ClassifiedListingDecodeTest {
  String eventJson =
      """
        {
          "id": "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07",
          "kind": 30402,
          "content": "classified content",
          "pubkey": "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984",
          "created_at": 1726114798510,
          "tags": [
            [ "e", "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346" ],
            [ "g", "classified geo-tag-1" ],
            [ "t", "classified hash-tag-1111" ],
            [ "price", "271.00", "BTC", "1" ],
            [ "p", "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984" ],
            [ "subject", "classified subject" ],
            [ "title", "classified title" ],
            [ "published_at", "1726114798510" ],
            [ "summary", "classified summary" ],
            [ "location", "classified peroulades" ]
          ],
          "sig": "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"
        }
      """;

  @Test
  void testClassifiedListingDecoding() {
    assertDoesNotThrow(
        () -> new GenericEventDecoder<>(ClassifiedListingEvent.class).decode(eventJson));
  }
}
