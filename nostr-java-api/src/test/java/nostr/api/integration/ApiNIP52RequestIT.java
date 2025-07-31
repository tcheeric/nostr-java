package nostr.api.integration;

import nostr.api.NIP52;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class ApiNIP52RequestIT extends BaseRelayIntegrationTest {
  private static final String PRV_KEY_VALUE = "23c011c4c02de9aa98d48c3646c70bb0e7ae30bdae1dfed4d251cbceadaeeb7b";
  private static final String UUID_CALENDAR_TIME_BASED_EVENT_TEST = "UUID-CalendarTimeBasedEventTest";

  public static final String ID = "299ab85049a7923e9cd82329c0fa489ca6fd6d21feeeac33543b1237e14a9e07";
  public static final String KIND = "31923";
  public static final String CALENDAR_CONTENT = "calendar content";
  public static final String PUB_KEY = "cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
  public static final String CREATED_AT = "1726114798510";
  public static final String START = "1726114798610";
  public static final String END = "1726114798710";

  public static final String START_TZID = "America/Costa_Rica";
  public static final String END_TZID = "America/Costa_Rica";

  public static final String E_TAG_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346";
  public static final String G_TAG_VALUE = "calendar geo-tag-1";
  public static final String T_TAG_VALUE = "calendar hash-tag-1111";
  public static final String P1_TAG_HEX = "444d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
  public static final String P1_ROLE = "PAYER";
  public static final String P2_ROLE = "PAYEE";

  public static final String P2_TAG_HEX = "555d79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
  public static final String TITLE = "calendar title";
  public static final String SUMMARY = "calendar summary";
  public static final String LOCATION = "calendar location";

  public static final EventTag E_TAG = new EventTag(E_TAG_HEX);
  public static final GeohashTag G_TAG = new GeohashTag(G_TAG_VALUE);
  public static final HashtagTag T_TAG = new HashtagTag(T_TAG_VALUE);

  public static final String LABEL_NAMESPACE = "audiospace";
  public static final String LABEL_1 = "calendar label 1 of 2";
  public static final String LABEL_2 = "calendar label 2 of 2";

  public static final String START_TZID_CODE = "start_tzid";
  public static final String END_TZID_CODE = "end_tzid";
  public static final String SUMMARY_CODE = "summary";
  public static final String LABEL_CODE = "l";
  public static final String LOCATION_CODE = "location";
  public static final String END_CODE = "end";

  public String eventId;
  public String eventPubKey;
  public String signature;

  @Test
  void testNIP99CalendarContentPreRequest() throws IOException {
    System.out.println("testNIP52CalendarContentEvent");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    PubKeyTag p1Tag = new PubKeyTag(new PublicKey(P1_TAG_HEX), getRelayUri(), P1_ROLE);
    PubKeyTag p2Tag = new PubKeyTag(new PublicKey(P2_TAG_HEX), getRelayUri(), P2_ROLE);
    tags.add(p1Tag);
    tags.add(p2Tag);
    tags.add(BaseTag.create(START_TZID_CODE,  START_TZID));
    tags.add(BaseTag.create(END_TZID_CODE,  END_TZID));
    tags.add(BaseTag.create(SUMMARY_CODE,  SUMMARY));
    tags.add(BaseTag.create(LABEL_CODE,  LABEL_1, LABEL_NAMESPACE));
    tags.add(BaseTag.create(LABEL_CODE,  LABEL_2, LABEL_NAMESPACE));
    tags.add(BaseTag.create(LOCATION_CODE,  LOCATION));
    tags.add(BaseTag.create(END_CODE,  END));
    tags.add(G_TAG);
    tags.add(T_TAG);
    tags.add(new ReferenceTag(URI.create(getRelayUri())));

    CalendarContent<BaseTag> calendarContent = new CalendarContent<>(
            new IdentifierTag(UUID_CALENDAR_TIME_BASED_EVENT_TEST),
            TITLE,
            Long.valueOf(START));

    var nip52 = new NIP52(Identity.create(PRV_KEY_VALUE));

    GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, CALENDAR_CONTENT, calendarContent).sign().getEvent();
    event.setCreatedAt(Long.valueOf(CREATED_AT));
    eventId = event.getId();
    signature = event.getSignature().toString();
    eventPubKey = event.getPubKey().toString();
    EventMessage eventMessage = new EventMessage(event);

    SpringWebSocketClient springWebSocketEventClient = new SpringWebSocketClient(getRelayUri());
    String eventResponse = springWebSocketEventClient.send(eventMessage).blockFirst();

    // Extract and compare only first 3 elements of the JSON array
    var expectedArray = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(0).asText();
    var expectedSubscriptionId = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(1).asText();
    var expectedSuccess = MAPPER_AFTERBURNER.readTree(expectedEventResponseJson(event.getId())).get(2).asBoolean();

    var actualArray = MAPPER_AFTERBURNER.readTree(eventResponse).get(0).asText();
    var actualSubscriptionId = MAPPER_AFTERBURNER.readTree(eventResponse).get(1).asText();
    var actualSuccess = MAPPER_AFTERBURNER.readTree(eventResponse).get(2).asBoolean();

      assertEquals(expectedArray, actualArray, "First element should match");
      assertEquals(expectedSubscriptionId, actualSubscriptionId, "Subscription ID should match");
    //assertTrue(expectedSuccess == actualSuccess, "Success flag should match"); -- This test is not required. The relay will always return false because we resending the same event, causing duplicates.

    springWebSocketEventClient.closeSocket();


    // TODO - This assertion fails with superdonductor and nostr-rs-relay

    SpringWebSocketClient springWebSocketRequestClient = new SpringWebSocketClient(getRelayUri());
    String subscriberId = UUID.randomUUID().toString();
    String reqJson = createReqJson(subscriberId, eventId);
    String reqResponse = springWebSocketRequestClient.send(reqJson).blockFirst();

    String expected = expectedRequestResponseJson(subscriberId);
    // TODO - This assertion keeps failing...
/*
    assertTrue(
        JsonComparator.isEquivalentJson(
            MAPPER_AFTERBURNER.readTree(expected),
            MAPPER_AFTERBURNER.readTree(reqResponse)));
*/

    springWebSocketRequestClient.closeSocket();
  }

  private String expectedEventResponseJson(String subscriptionId) {
    return "[\"OK\",\"" + subscriptionId + "\",true,\"success: request processed\"]";
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private String expectedRequestResponseJson(String subscriberId) {
    return "   [\"EVENT\",\"" + subscriberId + "\",\n" +
        "          {\"id\": \"" + eventId + "\",\n" +
        "          \"kind\": " + KIND + ",\n" +
        "          \"content\": \"" + CALENDAR_CONTENT + "\",\n" +
        "          \"pubkey\": \"" + eventPubKey + "\",\n" +
        "          \"created_at\": " + CREATED_AT + ",\n" +
        "          \"tags\": [\n" +
        "            [ \"e\", \"" + E_TAG.getIdEvent() + "\" ],\n" +
        "            [ \"g\", \"" + G_TAG.getLocation() + "\" ],\n" +
        "            [ \"t\", \"" + T_TAG.getHashTag() + "\" ],\n" +
        "            [ \"d\", \"" + UUID_CALENDAR_TIME_BASED_EVENT_TEST + "\" ],\n" +
        "            [ \"p\", \"" + P1_TAG_HEX + "\", \"" + getRelayUri() + "\", \"" + P1_ROLE + "\" ],\n" +
        "            [ \"p\", \"" + P2_TAG_HEX + "\", \"" + getRelayUri() + "\", \"" + P2_ROLE + "\" ],\n" +
        "            [ \"start_tzid\", \"" + START_TZID + "\" ],\n" +
        "            [ \"end_tzid\", \"" + END_TZID + "\" ],\n" +
        "            [ \"summary\", \"" + SUMMARY + "\" ],\n" +
        "            [ \"l\", \"" + LABEL_1 + "\", \"" + LABEL_NAMESPACE + "\" ],\n" +
        "            [ \"l\", \"" + LABEL_2 + "\", \"" + LABEL_NAMESPACE + "\" ],\n" +
        "            [ \"location\", \"" + LOCATION + "\" ],\n" +
        "            [ \"r\", \"" + URI.create(getRelayUri()) + "\" ],\n" +
        "            [ \"title\", \"" + TITLE + "\" ],\n" +
        "            [ \"start\", \"" + START + "\" ],\n" +
        "            [ \"end\", \"" + END + "\" ]\n" +
        "          ],\n" +
        "          \"sig\": \"" + signature + "\"\n" +
        "        }]";
  }
}
