package nostr.api.integration;

import com.fasterxml.jackson.databind.JsonNode;
import nostr.api.NIP99;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class ApiNIP99RequestIT {
  private static final String PRV_KEY_VALUE = "23c011c4c02de9aa98d48c3646c70bb0e7ae30bdae1dfed4d251cbceadaeeb7b";
  private static final String RELAY_URI = "ws://localhost:5555";
  public static final String PUBLISHED_AT_CODE = "published_at";
  public static final String LOCATION_CODE = "location";

  public static final String ID = "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07";
  public static final String KIND = "30402";
  public static final String CLASSIFIED_CONTENT = "classified content";
  public static final String PUB_KEY = "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
  public static final String CREATED_AT = "1726114798510";
  public static final String E_TAG_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
  public static final String G_TAG_VALUE = "classified geo-tag-1";
  public static final String T_TAG_VALUE = "classified hash-tag-1111";
  public static final String P_TAG_HEX = "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
  public static final String SUBJECT = "classified subject";
  public static final String TITLE = "classified title";
  public static final String SUMMARY = "classified summary";
  public static final String LOCATION = "classified location";

  public static final SubjectTag SUBJECT_TAG = new SubjectTag(SUBJECT);
  public static final EventTag E_TAG = new EventTag(E_TAG_HEX);
  public static final PubKeyTag P_TAG = new PubKeyTag(new PublicKey(P_TAG_HEX));
  public static final GeohashTag G_TAG = new GeohashTag(G_TAG_VALUE);
  public static final HashtagTag T_TAG = new HashtagTag(T_TAG_VALUE);

  public static final String PRICE_NUMBER = "271.00";
  public static final String CURRENCY = "BTC";
  public static final String FREQUENCY = "1";
  public static final BigDecimal NUMBER = new BigDecimal(PRICE_NUMBER);

  public String eventId;
  public Long eventCreatedAt;
  public String eventPubKey;
  public String signature;

  @Order(1)
  @Test
  void testNIP99ClassifiedListingPreRequest() throws IOException {
    System.out.println("testNIP99ClassifiedListingEvent");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P_TAG);
    tags.add(GenericTag.create(PUBLISHED_AT_CODE, 99, CREATED_AT));
    tags.add(GenericTag.create(LOCATION_CODE, 99, LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);

    PriceTag priceTag = new PriceTag(NUMBER, CURRENCY, FREQUENCY);
    ClassifiedListing classifiedListing = ClassifiedListing.builder(
            TITLE,
            SUMMARY,
            priceTag)
        .build();

    var nip99 = new NIP99<>(Identity.create(PRV_KEY_VALUE));

    GenericEvent event = nip99.createClassifiedListingEvent(tags, CLASSIFIED_CONTENT, classifiedListing).sign().getEvent();
    eventId = event.getId();
    eventCreatedAt = event.getCreatedAt();
    signature = event.getSignature().toString();
    eventPubKey = event.getPubKey().toString();
    EventMessage eventMessage = new EventMessage(event);

    SpringWebSocketClient springWebSocketEventClient = new SpringWebSocketClient(RELAY_URI);
    List<String> eventResponses = springWebSocketEventClient.send(eventMessage);

    assertTrue(eventResponses.size() == 1, "Expected 1 event response, but got " + eventResponses.size());

    // Extract and compare only first 3 elements of the JSON array
    var expectedArray = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(0).asText();
    var expectedSubscriptionId = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(1).asText();
    var expectedSuccess = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(2).asBoolean();

    var actualArray = MAPPER_AFTERBURNER.readTree(eventResponses.get(0)).get(0).asText();
    var actualSubscriptionId = MAPPER_AFTERBURNER.readTree(eventResponses.get(0)).get(1).asText();
    var actualSuccess = MAPPER_AFTERBURNER.readTree(eventResponses.get(0)).get(2).asBoolean();

    assertTrue(expectedArray.equals(actualArray), "First element should match");
    assertTrue(expectedSubscriptionId.equals(actualSubscriptionId), "Subscription ID should match");
    assertTrue(expectedSuccess == actualSuccess, "Success flag should match");

    springWebSocketEventClient.closeSocket();

    // TODO - Investigate why EOSE, instead of EVENT, is returned from nostr-rs-relay, and not superconductor

/*
    SpringWebSocketClient springWebSocketRequestClient = new SpringWebSocketClient(RELAY_URI);
    String reqJson = createReqJson(SUBSCRIBER_ID, eventId);
    List<String> reqResponses = springWebSocketRequestClient.send(reqJson).stream().toList();
    springWebSocketRequestClient.closeSocket();

    var actualJson = MAPPER_AFTERBURNER.readTree(reqResponses.getFirst());
    var expectedJson = MAPPER_AFTERBURNER.readTree(expectedRequestResponseJson());

    // Verify you receive the event
    assertTrue(actualJson.get(0).asText().equals("EVENT"), "Event should be received, and not " + actualJson.get(0).asText());

    // Verify only required fields
    assertTrue(actualJson.size() == 3, "Expected 3 elements in the array, but got " + actualJson.size());
    assertTrue(actualJson.get(2).get("id").asText().equals(expectedJson.get(2).get("id").asText()), "ID should match");
    assertTrue(actualJson.get(2).get("kind").asInt() == expectedJson.get(2).get("kind").asInt(), "Kind should match");

    // Verify required tags
    var actualTags = actualJson.get(2).get("tags");
    assertTrue(hasRequiredTag(actualTags, "price", NUMBER.toString()), "Price tag should be present");
    assertTrue(hasRequiredTag(actualTags, "title", TITLE), "Title tag should be present");
    assertTrue(hasRequiredTag(actualTags, "summary", SUMMARY), "Summary tag should be present");
*/
  }

  private String expectedEventResponseJson(String subscriptionId) {
    return "[\"OK\",\"" + subscriptionId + "\",true,\"success: request processed\"]";
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private String expectedRequestResponseJson() {
    return "   [\"EVENT\",\"ApiNIP99RequestTest-subscriber_001" + "\",\n" +
        "          {\"id\": \"" + eventId + "\",\n" +
        "          \"kind\": " + KIND + ",\n" +
        "          \"content\": \"" + CLASSIFIED_CONTENT + "\",\n" +
        "          \"pubkey\": \"" + eventPubKey + "\",\n" +
        "          \"created_at\": " + eventCreatedAt + ",\n" +
        "          \"tags\": [\n" +
        "            [ \"price\", \"" + NUMBER + "\", \"" + CURRENCY + "\", \"" + FREQUENCY + "\" ],\n" +
        "            [ \"title\", \"" + TITLE + "\" ],\n" +
        "            [ \"summary\", \"" + SUMMARY + "\" ]\n" +
        "          ],\n" +
        "          \"sig\": \"" + signature + "\"\n" +
        "        }]";
  }

  private boolean hasRequiredTag(JsonNode tags, String tagName, String expectedValue) {
    for (JsonNode tag : tags) {
      if (tag.get(0).asText().equals(tagName) && tag.get(1).asText().equals(expectedValue)) {
        return true;
      }
    }
    return false;
  }
}
