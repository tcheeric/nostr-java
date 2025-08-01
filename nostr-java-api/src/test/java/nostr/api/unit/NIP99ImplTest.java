package nostr.api.unit;

import nostr.api.NIP99;
import nostr.event.BaseTag;
import nostr.event.entities.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NIP99ImplTest {
  public static final String CONTENT = "ClassifiedListingEvent unit test content";
  public static final String UNIT_TEST_TITLE = "unit test title";
  public static final String UNIT_TEST_SUMMARY = "unit test summary";
  public static final String CURRENCY = "BTC";
  public static final String MONTH = "MONTH";
  public static final String LOCATION = "pangea";
  public static final PriceTag PRICE_TAG = new PriceTag(BigDecimal.valueOf(11111), CURRENCY, MONTH);
  public static final Long PUBLISHED_AT = 1716513986268L;
  static ClassifiedListing classifiedListing;
  static Identity sender;
  static NIP99 nip99;

  @BeforeAll
  static void setup() {
    classifiedListing = ClassifiedListing.builder(
            UNIT_TEST_TITLE,
            UNIT_TEST_SUMMARY,
            PRICE_TAG)
        .build();
    classifiedListing.setLocation(LOCATION);
    classifiedListing.setPublishedAt(PUBLISHED_AT);
    sender = Identity.generateRandomIdentity();
    nip99 = new NIP99(sender);
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithAllOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithAllOptionalParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    GenericEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    //instance.update();

    assertNotNull(instance.getId());
    assertTrue(instance.getTags().contains(containsGeneric("title", UNIT_TEST_TITLE)));
    assertTrue(instance.getTags().contains(containsGeneric("summary", UNIT_TEST_SUMMARY)));
    assertTrue(instance.getTags().contains(containsGeneric("published_at", PUBLISHED_AT.toString())));
    assertTrue(instance.getTags().contains(containsGeneric("location", LOCATION)));
    assertTrue(instance.getTags().contains(PRICE_TAG));

    ClassifiedListing classifiedListing2 = ClassifiedListing.builder(
            UNIT_TEST_TITLE,
            UNIT_TEST_SUMMARY,
            PRICE_TAG)
        .build();
    classifiedListing2.setLocation(LOCATION);
    classifiedListing2.setPublishedAt(PUBLISHED_AT);
    GenericEvent instance2 = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    //instance.update();

    assertNotNull(instance2.getId());
    assertEquals(instance.getPubKey(), instance2.getPubKey());
    assertEquals(instance.getKind(), instance2.getKind());
    assertEquals(instance.getTags(), instance2.getTags());
    assertEquals(instance.getContent(), instance2.getContent());
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithoutOptionalParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithoutOptionalParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();

    GenericEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    //instance.update();

    assertNotNull(instance.getId());
  }

  @Test
  void testNIP99CreateClassifiedListingEventWithDuplicateParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventWithDuplicateParameters");

    List<BaseTag> baseTags = new ArrayList<BaseTag>();

    var nip99 = new NIP99(sender);

    classifiedListing.setLocation(LOCATION);
    classifiedListing.setPublishedAt(PUBLISHED_AT);

    baseTags.add(BaseTag.create("published_at", String.valueOf(PUBLISHED_AT)));
    GenericEvent instance = nip99.createClassifiedListingEvent(baseTags, CONTENT, classifiedListing).getEvent();
    //instance.update();

    assertNotNull(instance.getId());
    assertTrue(instance.getTags().contains(containsGeneric("location", LOCATION)));
    assertTrue(instance.getTags().contains(containsGeneric("published_at", PUBLISHED_AT.toString())));
  }

  @Test
  void testNIP99CreateClassifiedListingEventNullParameters() {
    System.out.println("testNIP99CreateClassifiedListingEventNullParameters");
    assertThrows(NullPointerException.class, () -> ClassifiedListing.builder(null, UNIT_TEST_SUMMARY, PRICE_TAG).build());
    assertThrows(NullPointerException.class, () -> ClassifiedListing.builder(UNIT_TEST_TITLE, null, PRICE_TAG).build());
    assertThrows(NullPointerException.class, () -> ClassifiedListing.builder(UNIT_TEST_TITLE, UNIT_TEST_SUMMARY, null).build());
  }

  private BaseTag containsGeneric(String key, String value) {
    return BaseTag.create(key, value);
  }
}
