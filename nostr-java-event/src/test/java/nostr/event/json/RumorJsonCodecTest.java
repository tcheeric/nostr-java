package nostr.event.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.base.Kinds;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.Rumor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a rumor survives a JSON round trip with its id intact.
 *
 * <p>A rumor is serialized to JSON before being encrypted into a seal and parsed back out after
 * decryption, so any escaping mismatch between the two directions corrupts a message or breaks
 * its id. These tests exercise the content that a hand-written escaper typically mishandles.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/59.md">NIP-59</a>
 */
class RumorJsonCodecTest {

  private static final ObjectMapper MAPPER = EventJsonMapper.getMapper();

  private static final PublicKey AUTHOR =
      new PublicKey("611df01bfcf85c26ae65453b772d8f1dfd25c264621c0277e1fc1518686faef9");

  private static Rumor roundTrip(Rumor rumor) throws Exception {
    return MAPPER.readValue(MAPPER.writeValueAsString(rumor), Rumor.class);
  }

  /** A plain rumor survives serialization and deserialization unchanged. */
  @Test
  @DisplayName("round-trips a plain rumor unchanged")
  void roundTripsPlainRumor() throws Exception {
    Rumor original =
        Rumor.create(
            AUTHOR,
            Kinds.CHAT_MESSAGE,
            List.of(BaseTag.create("p", AUTHOR.toString())),
            "Are you going to the party tonight?");

    Rumor parsed = roundTrip(original);

    assertEquals(original, parsed);
    assertTrue(parsed.hasValidId());
  }

  /**
   * Content containing quotes, backslashes, newlines, control characters, and astral-plane
   * Unicode round-trips with a matching id. This is precisely where a hand-written escaper
   * fails, producing an event whose id does not match its content.
   */
  @Test
  @DisplayName("round-trips content that a naive escaper would corrupt")
  void roundTripsContentNeedingEscaping() throws Exception {
    String awkward =
        "quote\" backslash\\ slash/ newline\n carriage\r tab\t "
            + "control\u0001 null-ish\u0000 unicode\u00e9 astral\uD83D\uDE80";

    Rumor original = Rumor.create(AUTHOR, Kinds.CHAT_MESSAGE, List.of(), awkward);
    Rumor parsed = roundTrip(original);

    assertEquals(awkward, parsed.getContent());
    assertEquals(original.getId(), parsed.getId());
    assertTrue(parsed.hasValidId(), "id must still verify after a JSON round trip");
  }

  /** Tag values needing escaping survive the round trip, since tags contribute to the id. */
  @Test
  @DisplayName("round-trips tag values that need escaping")
  void roundTripsTagValuesNeedingEscaping() throws Exception {
    Rumor original =
        Rumor.create(
            AUTHOR,
            Kinds.CHAT_MESSAGE,
            List.of(BaseTag.create("subject", "re: \"dinner\"\tand\ndrinks")),
            "hello");

    Rumor parsed = roundTrip(original);

    assertEquals(original.getId(), parsed.getId());
    assertTrue(parsed.hasValidId());
  }

  /** An empty message body is valid and keeps a verifiable id. */
  @Test
  @DisplayName("round-trips empty content")
  void roundTripsEmptyContent() throws Exception {
    Rumor original = Rumor.create(AUTHOR, Kinds.CHAT_MESSAGE, List.of(), "");

    Rumor parsed = roundTrip(original);

    assertEquals("", parsed.getContent());
    assertTrue(parsed.hasValidId());
  }

  /** A large message, up to the NIP-44 maximum plaintext size, round-trips intact. */
  @Test
  @DisplayName("round-trips content at the NIP-44 maximum plaintext size")
  void roundTripsMaximumSizeContent() throws Exception {
    String large = "x".repeat(65_535);

    Rumor original = Rumor.create(AUTHOR, Kinds.CHAT_MESSAGE, List.of(), large);
    Rumor parsed = roundTrip(original);

    assertEquals(large.length(), parsed.getContent().length());
    assertTrue(parsed.hasValidId());
  }

  /**
   * The serialized form uses the wire field names from NIP-01 and carries no signature field,
   * since a rumor has none to carry.
   */
  @Test
  @DisplayName("serializes NIP-01 wire field names and no signature")
  void serializesWireFieldNames() throws Exception {
    Rumor rumor = Rumor.create(AUTHOR, Kinds.CHAT_MESSAGE, List.of(), "hello");

    String json = MAPPER.writeValueAsString(rumor);

    assertTrue(json.contains("\"pubkey\""));
    assertTrue(json.contains("\"created_at\""));
    assertTrue(json.contains("\"kind\""));
    assertTrue(json.contains("\"tags\""));
    assertTrue(json.contains("\"content\""));
    assertFalse(json.contains("\"sig\""), "a rumor must never carry a signature");
  }

  /** A rumor arriving without an id gets one derived, so incoming events are always identified. */
  @Test
  @DisplayName("derives an id when the incoming JSON omits one")
  void derivesIdWhenAbsentFromJson() throws Exception {
    String json =
        "{\"pubkey\":\""
            + AUTHOR
            + "\",\"created_at\":1691518405,\"kind\":1,\"tags\":[],"
            + "\"content\":\"Are you going to the party tonight?\"}";

    Rumor parsed = MAPPER.readValue(json, Rumor.class);

    assertEquals(
        "9dd003c6d3b73b74a85a9ab099469ce251653a7af76f523671ab828acd2a0ef9", parsed.getId());
  }
}
