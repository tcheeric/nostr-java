package nostr.test.event;

import nostr.api.NIP99;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class NIP99ImplTest {
  @Test
  void testNIP99CreateClassifiedListingEvent() {
    System.out.println("testNIP99CreateClassifiedListingEvent");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    final String CONTENT = "ClassifiedListingEvent unit test content";

    var nip99 = new NIP99<ClassifiedListingEvent>(sender);
    String unitTestTitle = "unit test title";
    String unitTestSummary = "unit test summary";
    String currency = "BTC";
    String month = "MONTH";

    ClassifiedListing classifiedListing = new ClassifiedListing(
        unitTestTitle,
        unitTestSummary,
        new PriceTag(BigDecimal.valueOf(11111), currency, month)
    );

    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    Assertions.assertNotNull(instance.getId());
  }
}
