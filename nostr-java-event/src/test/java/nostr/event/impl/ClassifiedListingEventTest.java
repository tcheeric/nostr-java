package nostr.event.impl;

import nostr.base.Kind;
import nostr.base.PublicKey;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassifiedListingEventTest {

  // Verifies only allowed kinds (30402, 30403) pass validation.
  @Test
  void validateKindAllowsOnlyNip99Values() {
    PublicKey pk = new PublicKey("e4343c157d026999e106b3bc4245b6c87f52cc8050c4c3b2f34b3567a04ccf95");

    ClassifiedListingEvent active =
        new ClassifiedListingEvent(pk, Kind.CLASSIFIED_LISTING, List.of(), "");
    ClassifiedListingEvent inactive =
        new ClassifiedListingEvent(pk, Kind.CLASSIFIED_LISTING_INACTIVE, List.of(), "");

    assertDoesNotThrow(active::validateKind);
    assertDoesNotThrow(inactive::validateKind);
  }

  // Ensures other kinds fail validation.
  @Test
  void validateKindRejectsInvalidValues() {
    PublicKey pk = new PublicKey("e4343c157d026999e106b3bc4245b6c87f52cc8050c4c3b2f34b3567a04ccf95");
    ClassifiedListingEvent invalid =
        new ClassifiedListingEvent(pk, Kind.TEXT_NOTE, List.of(), "");
    assertThrows(AssertionError.class, invalid::validateKind);
  }
}

