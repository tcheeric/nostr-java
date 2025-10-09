package nostr.event.support;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.impl.GenericEvent;
import nostr.event.json.EventJsonMapper;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Test;

/** Tests for GenericEventSerializer, Updater and Validator utility classes. */
public class GenericEventSupportTest {

  private static final String HEX64 = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
  private static final String HEX128 = HEX64 + HEX64;

  private GenericEvent newEvent() {
    return GenericEvent.builder()
        .pubKey(new PublicKey(HEX64))
        .kind(Kind.TEXT_NOTE)
        .content("hello")
        .build();
  }

  @Test
  void serializerProducesCanonicalArray() throws Exception {
    GenericEvent event = newEvent();
    String json = GenericEventSerializer.serialize(event);
    // Expect leading 0, pubkey, created_at (may be null), kind, tags array, content string
    assertTrue(json.startsWith("["));
    assertTrue(json.contains("\"" + event.getPubKey().toString() + "\""));
    assertTrue(json.contains("\"hello\""));
  }

  @Test
  void updaterComputesIdAndSerializedCache() {
    GenericEvent event = newEvent();
    GenericEventUpdater.refresh(event);
    assertNotNull(event.getId());
    assertNotNull(event.getSerializedEventCache());
    // Recompute hash from serializer and compare
    String serialized = new String(event.getSerializedEventCache(), StandardCharsets.UTF_8);
    String expected = NostrUtil.bytesToHex(NostrUtil.sha256(serialized.getBytes(StandardCharsets.UTF_8)));
    assertEquals(expected, event.getId());
  }

  @Test
  void validatorAcceptsWellFormedEvent() throws Exception {
    GenericEvent event = newEvent();
    // set required id and signature fields (hex format only)
    GenericEventUpdater.refresh(event);
    event.setSignature(Signature.fromString(HEX128));
    // serialize to produce id
    event.setId(NostrUtil.bytesToHex(NostrUtil.sha256(GenericEventSerializer.serialize(event).getBytes(StandardCharsets.UTF_8))));
    assertDoesNotThrow(() -> GenericEventValidator.validate(event));
  }

  @Test
  void validatorRejectsInvalidFields() {
    GenericEvent event = newEvent();
    // Missing id/signature
    assertThrows(AssertionError.class, () -> GenericEventValidator.validate(event));
  }
}

