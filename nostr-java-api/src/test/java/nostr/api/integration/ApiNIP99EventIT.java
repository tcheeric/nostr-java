package nostr.api.integration;

import nostr.api.NIP99;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.entities.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class ApiNIP99EventIT extends BaseRelayIntegrationTest {
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

  public static final String CLASSIFIED_LISTING_PUBLISHED_AT = "1687765220";
  public static final String CLASSIFIED_LISTING_LOCATION = "classified listing location";
  public static final String TITLE_CODE = "title";
  public static final String SUMMARY_CODE = "summary";
  public static final String PUBLISHED_AT_CODE = "published_at";
  public static final String LOCATION_CODE = "location";
  private SpringWebSocketClient springWebSocketClient;

  @BeforeEach
  void setup() {
    springWebSocketClient = new SpringWebSocketClient(getRelayUri());
  }

  @Test
  void testNIP99ClassifiedListingEvent() throws IOException {
    System.out.println("testNIP99ClassifiedListingEvent");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P_TAG);
    tags.add(BaseTag.create(PUBLISHED_AT_CODE,  CLASSIFIED_LISTING_PUBLISHED_AT));
    tags.add(BaseTag.create(LOCATION_CODE,  CLASSIFIED_LISTING_LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);

    PriceTag priceTag = new PriceTag(NUMBER, CURRENCY, FREQUENCY);
    ClassifiedListing classifiedListing = ClassifiedListing.builder(
            TITLE_CODE,
            SUMMARY_CODE,
            priceTag)
        .build();

    classifiedListing.setPublishedAt(Long.parseLong(CLASSIFIED_LISTING_PUBLISHED_AT));

    var nip99 = new NIP99(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip99.createClassifiedListingEvent(tags, CLASSIFIED_LISTING_CONTENT, classifiedListing).sign().getEvent();
    EventMessage message = new EventMessage(event);

    // Extract and compare only first 3 elements of the JSON array
    var expectedArray = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(0).asText();
    var expectedSubscriptionId = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(1).asText();
    var expectedSuccess = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(2).asBoolean();

    String eventResponse = springWebSocketClient.send(message).stream().findFirst().get();
    var actualArray = MAPPER_AFTERBURNER.readTree(eventResponse).get(0).asText();
    var actualSubscriptionId = MAPPER_AFTERBURNER.readTree(eventResponse).get(1).asText();
    var actualSuccess = MAPPER_AFTERBURNER.readTree(eventResponse).get(2).asBoolean();

      assertEquals(expectedArray, actualArray, "First element should match");
      assertEquals(expectedSubscriptionId, actualSubscriptionId, "Subscription ID should match");
      assertEquals(expectedSuccess, actualSuccess, "Success flag should match");

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
