package nostr.test.event;

import nostr.api.NIP99;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class NIP99ImplTest {

  @Test
  void testNIP99CreateClassifiedListingEventWithAllOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithAllOptionalParameters");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    final String CONTENT = "ClassifiedListingEvent unit test content";

    var nip99 = new NIP99<ClassifiedListingEvent>(sender);
    String unitTestTitle = "unit test title";
    String unitTestSummary = "unit test summary";
    String currency = "BTC";
    String month = "MONTH";
    String location = "pangea";
    Long publishedAt = 1716513986268L;

    ClassifiedListing classifiedListing = new ClassifiedListing(
        unitTestTitle,
        unitTestSummary,
        new PriceTag(BigDecimal.valueOf(11111), currency, month)
    );
    classifiedListing.setLocation(location);
    classifiedListing.setPublishedAt(publishedAt);

    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    Assertions.assertNotNull(instance.getId());
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithoutOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithoutOptionalParameters");

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

  @Test
  void testNIP99CreateClassifiedListingEventWithDuplicateParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithDuplicateParameters");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    final String CONTENT = "ClassifiedListingEvent unit test content";

    var nip99 = new NIP99<ClassifiedListingEvent>(sender);
    String unitTestTitle = "unit test title";
    String unitTestSummary = "unit test summary";
    String currency = "BTC";
    String month = "MONTH";
    String location = "pangea";
    Long publishedAt = 1716513986268L;

    ClassifiedListing classifiedListing = new ClassifiedListing(
        unitTestTitle,
        unitTestSummary,
        new PriceTag(BigDecimal.valueOf(11111), currency, month)
    );
    classifiedListing.setLocation(location);
    classifiedListing.setPublishedAt(publishedAt);

    baseTags.add(GenericTag.create("published_at", 99, String.valueOf(publishedAt)));
    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    Assertions.assertNotNull(instance.getId());
  }
}
