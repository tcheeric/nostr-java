package nostr.id;

import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassifiedListingEventTest {
  public static final PublicKey senderPubkey = new PublicKey(Identity.generateRandomIdentity().getPublicKey().toString());
  public static final String CLASSIFIED_LISTING_CONTENT = "classified listing content";

  public static final String PTAG_HEX = "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985";
  public static final String ETAG_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347";

  public static final PubKeyTag P_TAG = new PubKeyTag(new PublicKey(PTAG_HEX));
  public static final EventTag E_TAG = new EventTag(ETAG_HEX);

  public static final String SUBJECT = "Classified Listing Test Subject Tag";
  public static final SubjectTag SUBJECT_TAG = new SubjectTag(SUBJECT);
  public static final GeohashTag G_TAG = new GeohashTag("Classified Listing Test Geohash Tag");
  public static final HashtagTag T_TAG = new HashtagTag("Classified Listing Test Hashtag Tag");

  public static final BigDecimal NUMBER = new BigDecimal("2.71");
  public static final String FREQUENCY = "NANOSECOND";
  public static final String CURRENCY = "BTC";
  public static final PriceTag PRICE_TAG = new PriceTag(NUMBER, CURRENCY, FREQUENCY);

  public static final String CLASSIFIED_LISTING_TITLE = "classified listing title";
  public static final String CLASSIFIED_LISTING_SUMMARY = "classified listing summary";
  public static final String CLASSIFIED_LISTING_PUBLISHED_AT = "1687765220";
  public static final String CLASSIFIED_LISTING_LOCATION = "classified listing location";
  public static final String TITLE_CODE = "title";
  public static final String SUMMARY_CODE = "summary";
  public static final String PUBLISHED_AT_CODE = "published_at";
  public static final String LOCATION_CODE = "location";

  private ClassifiedListingEvent instance;

  @BeforeAll
  void setup() {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P_TAG);
    tags.add(GenericTag.create(TITLE_CODE, CLASSIFIED_LISTING_TITLE));
    tags.add(GenericTag.create(SUMMARY_CODE, CLASSIFIED_LISTING_SUMMARY));
    tags.add(GenericTag.create(PUBLISHED_AT_CODE, CLASSIFIED_LISTING_PUBLISHED_AT));
    tags.add(GenericTag.create(LOCATION_CODE, CLASSIFIED_LISTING_LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);
    tags.add(PRICE_TAG);
    instance = new ClassifiedListingEvent(senderPubkey, tags, CLASSIFIED_LISTING_CONTENT, TITLE_CODE, SUMMARY_CODE, NUMBER, CURRENCY, FREQUENCY);
    instance.setSignature(Identity.generateRandomIdentity().sign(instance));
  }

  @Test
  void testConstructClassifiedListingEvent() {
    System.out.println("testConstructClassifiedListingEvent");

    assertEquals(13, instance.getTags().size());
    assertEquals(CLASSIFIED_LISTING_CONTENT, instance.getContent());
    assertEquals(Kind.CLASSIFIED_LISTING.getValue(), instance.getKind().intValue());
    assertEquals(senderPubkey.toString(), instance.getPubKey().toString());
    assertEquals(senderPubkey.toBech32String(), instance.getPubKey().toBech32String());
    assertEquals(senderPubkey.toHexString(), instance.getPubKey().toHexString());
    assertEquals(CLASSIFIED_LISTING_CONTENT, instance.getContent());
  }
}
