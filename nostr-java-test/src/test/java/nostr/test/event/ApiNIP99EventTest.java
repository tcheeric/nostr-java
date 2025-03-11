package nostr.test.event;

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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static nostr.test.event.ClassifiedListingEventTest.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Extract and compare only first 3 elements of the JSON array
    var expectedArray = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(0).asText();
    var expectedSubscriptionId = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(1).asText();
    var expectedSuccess = MAPPER_AFTERBURNER.readTree(expectedResponseJson(event.getId())).get(2).asBoolean();

    String eventResponse = springWebSocketClient.send(message).stream().findFirst().get();
    var actualArray = MAPPER_AFTERBURNER.readTree(eventResponse).get(0).asText();
    var actualSubscriptionId = MAPPER_AFTERBURNER.readTree(eventResponse).get(1).asText();
    var actualSuccess = MAPPER_AFTERBURNER.readTree(eventResponse).get(2).asBoolean();

    assertTrue(expectedArray.equals(actualArray), "First element should match");
    assertTrue(expectedSubscriptionId.equals(actualSubscriptionId), "Subscription ID should match");
    assertTrue(expectedSuccess == actualSuccess, "Success flag should match");

    springWebSocketClient.closeSocket();
  }

  private String expectedResponseJson(String sha256) {
    return "[\"OK\",\"" + sha256 + "\",true,\"success: request processed\"]";
  }
}
