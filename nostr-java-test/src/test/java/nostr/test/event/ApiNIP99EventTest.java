package nostr.test.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import nostr.api.NIP99;
import nostr.base.PrivateKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.message.EventMessage;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import static nostr.test.event.ClassifiedListingEventTest.CLASSIFIED_LISTING_CONTENT;
import static nostr.test.event.ClassifiedListingEventTest.CLASSIFIED_LISTING_LOCATION;
import static nostr.test.event.ClassifiedListingEventTest.CLASSIFIED_LISTING_PUBLISHED_AT;
import static nostr.test.event.ClassifiedListingEventTest.CURRENCY;
import static nostr.test.event.ClassifiedListingEventTest.E_TAG;
import static nostr.test.event.ClassifiedListingEventTest.FREQUENCY;
import static nostr.test.event.ClassifiedListingEventTest.G_TAG;
import static nostr.test.event.ClassifiedListingEventTest.LOCATION_CODE;
import static nostr.test.event.ClassifiedListingEventTest.NUMBER;
import static nostr.test.event.ClassifiedListingEventTest.PUBLISHED_AT_CODE;
import static nostr.test.event.ClassifiedListingEventTest.P_TAG;
import static nostr.test.event.ClassifiedListingEventTest.SUBJECT_TAG;
import static nostr.test.event.ClassifiedListingEventTest.SUMMARY_CODE;
import static nostr.test.event.ClassifiedListingEventTest.TITLE_CODE;
import static nostr.test.event.ClassifiedListingEventTest.T_TAG;

class ApiNIP99EventTest {
  private static final String RELAY_URI = "ws://localhost:5555";
  private final SpringWebSocketClient springWebSocketClient;

  public ApiNIP99EventTest() {
    springWebSocketClient = new SpringWebSocketClient(RELAY_URI);
  }

  @Test
  void testNIP99ClassifiedListingEvent() throws IOException {
    System.out.println("testNIP99ClassifiedListingEvent");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P_TAG);
    tags.add(GenericTag.create(PUBLISHED_AT_CODE, 99, CLASSIFIED_LISTING_PUBLISHED_AT));
    tags.add(GenericTag.create(LOCATION_CODE, 99, CLASSIFIED_LISTING_LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);

    PriceTag priceTag = new PriceTag(NUMBER, CURRENCY, FREQUENCY);
    ClassifiedListing classifiedListing = ClassifiedListing.builder(
            TITLE_CODE,
            SUMMARY_CODE,
            priceTag)
        .build();

    var nip99 = new NIP99<>(Identity.create(PrivateKey.generateRandomPrivKey()));

    GenericEvent event = nip99.createClassifiedListingEvent(tags, CLASSIFIED_LISTING_CONTENT, classifiedListing).sign().getEvent();
    EventMessage message = new EventMessage(event);

    ObjectMapper mapper = new ObjectMapper();

    // Extract and compare only first 3 elements of the JSON array
    var expectedArray = mapper.readTree(expectedResponseJson(event.getId())).get(0).asText();
    var expectedSubscriptionId = mapper.readTree(expectedResponseJson(event.getId())).get(1).asText();
    var expectedSuccess = mapper.readTree(expectedResponseJson(event.getId())).get(2).asBoolean();

    String eventResponse = springWebSocketClient.send(message).stream().findFirst().get();
    var actualArray = mapper.readTree(eventResponse).get(0).asText();
    var actualSubscriptionId = mapper.readTree(eventResponse).get(1).asText();
    var actualSuccess = mapper.readTree(eventResponse).get(2).asBoolean();

    assertTrue(expectedArray.equals(actualArray), "First element should match");
    assertTrue(expectedSubscriptionId.equals(actualSubscriptionId), "Subscription ID should match");
    assertTrue(expectedSuccess == actualSuccess, "Success flag should match");
    
    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
