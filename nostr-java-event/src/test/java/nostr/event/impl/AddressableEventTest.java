package nostr.event.impl;

import nostr.base.PublicKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for AddressableEvent kind validation per NIP-01. */
public class AddressableEventTest {

  @Test
  void validKind_30000_shouldPass() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 30_000, new ArrayList<>(), "");
    assertDoesNotThrow(event::validateKind);
  }

  @Test
  void validKind_35000_shouldPass() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 35_000, new ArrayList<>(), "");
    assertDoesNotThrow(event::validateKind);
  }

  @Test
  void validKind_39999_shouldPass() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 39_999, new ArrayList<>(), "");
    assertDoesNotThrow(event::validateKind);
  }

  @Test
  void invalidKind_29999_shouldFail() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 29_999, new ArrayList<>(), "");
    AssertionError error = assertThrows(AssertionError.class, event::validateKind);
    assertTrue(error.getMessage().contains("30000") && error.getMessage().contains("40000"));
  }

  @Test
  void invalidKind_40000_shouldFail() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 40_000, new ArrayList<>(), "");
    AssertionError error = assertThrows(AssertionError.class, event::validateKind);
    assertTrue(error.getMessage().contains("30000") && error.getMessage().contains("40000"));
  }

  @Test
  void invalidKind_0_shouldFail() {
    PublicKey pubKey = createDummyPublicKey();
    AddressableEvent event = new AddressableEvent(pubKey, 0, new ArrayList<>(), "");
    AssertionError error = assertThrows(AssertionError.class, event::validateKind);
    assertTrue(error.getMessage().contains("30000") && error.getMessage().contains("40000"));
  }

  private PublicKey createDummyPublicKey() {
    byte[] keyBytes = new byte[32];
    for (int i = 0; i < 32; i++) {
      keyBytes[i] = (byte) i;
    }
    return new PublicKey(keyBytes);
  }
}
