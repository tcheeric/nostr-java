package nostr.test.event;

import nostr.api.NIP99;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.ClassifiedListingEvent.ClassifiedListing;
import nostr.event.impl.GenericTag;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NIP99ImplTest {
  public final static String CONTENT = "ClassifiedListingEvent unit test content";
  public final static String UNIT_TEST_TITLE = "unit test title";
  public final static String UNIT_TEST_SUMMARY = "unit test summary";
  public final static String CURRENCY = "BTC";
  public final static String MONTH = "MONTH";
  public final static String LOCATION = "pangea";
  public final static PriceTag PRICE_TAG = new PriceTag(BigDecimal.valueOf(11111), CURRENCY, MONTH);
  public final static Long PUBLISHED_AT = 1716513986268L;
  static ClassifiedListing classifiedListing;
  static Identity sender;
  static NIP99<ClassifiedListingEvent> nip99;

  @BeforeAll
  static void setup() {
    classifiedListing = new ClassifiedListing(
        UNIT_TEST_TITLE,
        UNIT_TEST_SUMMARY,
        PRICE_TAG
    );
    classifiedListing.setLocation(LOCATION);
    classifiedListing.setPublishedAt(PUBLISHED_AT);
    sender = Identity.generateRandomIdentity();
    nip99 = new NIP99<>(sender);
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithAllOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithAllOptionalParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    assertNotNull(instance.getId());
    assertNull(instance.getClassifiedListing().getId());
    assertEquals(UNIT_TEST_TITLE, instance.getClassifiedListing().getTitle());
    assertEquals(UNIT_TEST_SUMMARY, instance.getClassifiedListing().getSummary());
    assertEquals(PUBLISHED_AT, instance.getClassifiedListing().getPublishedAt());
    assertEquals(LOCATION, instance.getClassifiedListing().getLocation());
    assertEquals(PRICE_TAG, instance.getClassifiedListing().getPriceTag());

    ClassifiedListing classifiedListing2 = new ClassifiedListing(
        UNIT_TEST_TITLE,
        UNIT_TEST_SUMMARY,
        PRICE_TAG
    );
    classifiedListing2.setLocation(LOCATION);
    classifiedListing2.setPublishedAt(PUBLISHED_AT);
    ClassifiedListingEvent instance2 = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    assertEquals(instance, instance2);
    assertEquals(instance.getClassifiedListing(), instance2.getClassifiedListing());
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithoutOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithoutOptionalParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();

    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    assertNotNull(instance.getId());
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithDuplicateParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithDuplicateParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();

    var nip99 = new NIP99<ClassifiedListingEvent>(sender);

    classifiedListing.setLocation(LOCATION);
    classifiedListing.setPublishedAt(PUBLISHED_AT);

    baseTags.add(GenericTag.create("published_at", 99, String.valueOf(PUBLISHED_AT)));
    ClassifiedListingEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    instance.update();

    assertNotNull(instance.getId());
    assertEquals(LOCATION, instance.getClassifiedListing().getLocation());
    assertEquals(PUBLISHED_AT, instance.getClassifiedListing().getPublishedAt());
  }

  @Test
  void testNIP99CreateClassifiedListingEventNullParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventNullParameters");
    assertThrows(NullPointerException.class, () -> new ClassifiedListing(null, UNIT_TEST_SUMMARY, PRICE_TAG));
    assertThrows(NullPointerException.class, () -> new ClassifiedListing(UNIT_TEST_TITLE, null, PRICE_TAG));
    assertThrows(NullPointerException.class, () -> new ClassifiedListing(UNIT_TEST_TITLE, UNIT_TEST_SUMMARY, null));
  }
}
