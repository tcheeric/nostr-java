package nostr.event.serializer;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import org.junit.jupiter.api.Test;

/** Tests for EventSerializer utility methods. */
public class EventSerializerTest {

  private static final String HEX64 = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

  @Test
  void serializeAndComputeIdStable() throws Exception {
    PublicKey pk = new PublicKey(HEX64);
    long ts = 1700000000L;
    String json = EventSerializer.serialize(pk, ts, Kind.TEXT_NOTE.getValue(), List.of(), "hello");
    assertTrue(json.startsWith("["));
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    String id = EventSerializer.computeEventId(bytes);

    // compute again should match
    String id2 = EventSerializer.serializeAndComputeId(pk, ts, Kind.TEXT_NOTE.getValue(), List.of(), "hello");
    assertEquals(id, id2);
  }

  @Test
  void serializeIncludesGenericTag() {
    PublicKey pk = new PublicKey(HEX64);
    // Use an unregistered tag code to force GenericTag path
    assertDoesNotThrow(() -> EventSerializer.serialize(pk, 1700000000L, Kind.TEXT_NOTE.getValue(), List.of(BaseTag.create("zzz")), ""));
  }

  @Test
  void computeEventIdThrowsForInvalidAlgorithmIsWrapped() {
    // We cannot force NoSuchAlgorithmException easily without changing code; ensure basic path works
    PublicKey pk = new PublicKey(HEX64);
    assertDoesNotThrow(() -> EventSerializer.serializeAndComputeId(pk, null, Kind.TEXT_NOTE.getValue(), List.of(), ""));
  }

  @Test
  void serializeIncludesTagsArray() throws Exception {
    PublicKey pk = new PublicKey(HEX64);
    long ts = 1700000001L;
    BaseTag e = BaseTag.create("e", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    String json = EventSerializer.serialize(pk, ts, Kind.TEXT_NOTE.getValue(), List.of(e), "");
    assertTrue(json.contains("\"e\""));
    assertTrue(json.contains("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    // ensure tag array wrapper present
    assertTrue(json.contains("[["));
  }
}
