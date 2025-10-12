package nostr.event.impl;

import nostr.base.PublicKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EphemeralEventTest {

  // Validates that kinds in [20000, 30000) are accepted.
  @Test
  void validateKindAllowsEphemeralRange() {
    PublicKey pk = new PublicKey("e4343c157d026999e106b3bc4245b6c87f52cc8050c4c3b2f34b3567a04ccf95");

    EphemeralEvent k20000 = new EphemeralEvent(pk, 20_000, List.of(), "");
    EphemeralEvent k29999 = new EphemeralEvent(pk, 29_999, List.of(), "");

    assertDoesNotThrow(k20000::validateKind);
    assertDoesNotThrow(k29999::validateKind);
  }

  // Ensures values outside the range are rejected.
  @Test
  void validateKindRejectsOutOfRange() {
    PublicKey pk = new PublicKey("e4343c157d026999e106b3bc4245b6c87f52cc8050c4c3b2f34b3567a04ccf95");
    EphemeralEvent below = new EphemeralEvent(pk, 19_999, List.of(), "");
    EphemeralEvent atUpper = new EphemeralEvent(pk, 30_000, List.of(), "");

    assertThrows(AssertionError.class, below::validateKind);
    assertThrows(AssertionError.class, atUpper::validateKind);
  }
}

