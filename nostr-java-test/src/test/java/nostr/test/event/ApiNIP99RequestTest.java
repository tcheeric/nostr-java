package nostr.test.event;

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
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static nostr.test.event.ClassifiedListingEventTest.LOCATION_CODE;
import static nostr.test.event.ClassifiedListingEventTest.PUBLISHED_AT_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiNIP99RequestTest implements Subscriber<String> {
  private static final String PRV_KEY_VALUE = "23c011c4c02de9aa98d48c3646c70bb0e7ae30bdae1dfed4d251cbceadaeeb7b";
  private static final String RELAY_URI = "ws://localhost:5555";
  private static final String SUBSCRIBER_ID = "ApiNIP99RequestTest-subscriber_001";
  private Subscription subscription;
  private String relayEventResponse = null;

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
    EventMessage eventMessage = new EventMessage(event, eventId);

    SpringWebSocketClient springWebSocketEventClient = new SpringWebSocketClient(RELAY_URI);
    String eventResponse = springWebSocketEventClient.send(eventMessage).stream().findFirst().get();
    assertEquals(
        removeWhiteSpace(expectedEventResponseJson(event.getId())),
        removeWhiteSpace(eventResponse)
    );
    springWebSocketEventClient.closeSocket();

    SpringWebSocketClient springWebSocketRequestClient = new SpringWebSocketClient(RELAY_URI);
    String reqJson = createReqJson(SUBSCRIBER_ID, eventId);
    String reqResponse = springWebSocketRequestClient.send(reqJson).stream().findFirst().get();
    assertEquals(
        removeWhiteSpace(expectedRequestResponseJson()),
        removeWhiteSpace(reqResponse)
    );
    springWebSocketRequestClient.closeSocket();
  }


  @Override
  public void onSubscribe(Subscription subscription) {
    relayEventResponse = null;
    this.subscription = subscription;
    subscription.request(10);
  }

  @Override
  public void onNext(String s) {
    subscription.request(10);
    relayEventResponse = s;
  }

  @Override
  public void onError(Throwable throwable) {
  }

  @Override
  public void onComplete() {
  }

  private String expectedEventResponseJson(String subscriptionId) {
    return "[\"OK\",\"" + subscriptionId + "\",true,\"success: request processed\"]";
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private String expectedRequestResponseJson() {
    return
        "   [\"EVENT\",\"ApiNIP99RequestTest-subscriber_001" + "\",\n" +
            "          {\"id\": \"" + eventId + "\",\n" +
            "          \"kind\": " + KIND + ",\n" +
            "          \"content\": \"" + CLASSIFIED_CONTENT + "\",\n" +
            "          \"pubkey\": \"" + eventPubKey + "\",\n" +
            "          \"created_at\": " + eventCreatedAt + ",\n" +
            "          \"tags\": [\n" +
            "            [ \"e\", \"" + E_TAG.getIdEvent() + "\", \"" + E_TAG.getMarker() + "\" ],\n" +
            "            [ \"g\", \"" + G_TAG.getLocation() + "\" ],\n" +
            "            [ \"t\", \"" + T_TAG.getHashTag() + "\" ],\n" +
            "            [ \"price\", \"" + NUMBER + "\", \"" + CURRENCY + "\", \"" + FREQUENCY + "\" ],\n" +
            "            [ \"p\", \"" + P_TAG.getPublicKey() + "\" ],\n" +
            "            [ \"subject\", \"" + SUBJECT + "\" ],\n" +
            "            [ \"published_at\", \"" + CREATED_AT + "\" ],\n" +
            "            [ \"location\", \"" + LOCATION + "\" ],\n" +
            "            [ \"title\", \"" + TITLE + "\" ],\n" +
            "            [ \"summary\", \"" + SUMMARY + "\" ]\n" +
            "          ],\n" +
            "          \"sig\": \"" + signature + "\"\n" +
            "        }]";
  }

  private String removeWhiteSpace(String input) {
    return input.replaceAll("\\s+", "");
  }
}
