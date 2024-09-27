package nostr.test.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.api.NIP52;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.id.Identity;
import nostr.test.util.JsonComparator;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiNIP52RequestTest {
  private static final String PRV_KEY_VALUE = "23c011c4c02de9aa98d48c3646c70bb0e7ae30bdae1dfed4d251cbceadaeeb7b";
  private static final String RELAY_URI = "ws://localhost:5555";
  private static final String SUBSCRIBER_ID = "ApiNIP52RequestTest-subscriber_001";
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
  public static final PubKeyTag P1_TAG = new PubKeyTag(new PublicKey(P1_TAG_HEX), RELAY_URI, P1_ROLE);
  public static final PubKeyTag P2_TAG = new PubKeyTag(new PublicKey(P2_TAG_HEX), RELAY_URI, P2_ROLE);
  public static final GeohashTag G_TAG = new GeohashTag(G_TAG_VALUE);
  public static final HashtagTag T_TAG = new HashtagTag(T_TAG_VALUE);
  public static final ReferenceTag R_TAG = new ReferenceTag(URI.create(RELAY_URI));

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

  @Order(1)
  @Test
  void testNIP99CalendarContentPreRequest() throws IOException {
    System.out.println("testNIP52CalendarContentEvent");

    List<BaseTag> tags = new ArrayList<>();
    tags.add(E_TAG);
    tags.add(P1_TAG);
    tags.add(P2_TAG);
    tags.add(GenericTag.create(START_TZID_CODE, 52, START_TZID));
    tags.add(GenericTag.create(END_TZID_CODE, 52, END_TZID));
    tags.add(GenericTag.create(SUMMARY_CODE, 52, SUMMARY));
    tags.add(GenericTag.create(LABEL_CODE, 52, LABEL_1));
    tags.add(GenericTag.create(LABEL_CODE, 52, LABEL_2));
    tags.add(GenericTag.create(LOCATION_CODE, 52, LOCATION));
    tags.add(GenericTag.create(END_CODE, 52, END));
    tags.add(G_TAG);
    tags.add(T_TAG);
    tags.add(R_TAG);

    CalendarContent calendarContent = CalendarContent.builder(
            new IdentifierTag(UUID_CALENDAR_TIME_BASED_EVENT_TEST),
            TITLE,
            Long.valueOf(START))
        .build();

    var nip52 = new NIP52<>(Identity.create(PRV_KEY_VALUE));

    GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, CALENDAR_CONTENT, calendarContent).sign().getEvent();
    eventId = event.getId();
    event.setCreatedAt(Long.valueOf(CREATED_AT));
    signature = event.getSignature().toString();
    eventPubKey = event.getPubKey().toString();
    EventMessage eventMessage = new EventMessage(event, eventId);

    SpringWebSocketClient springWebSocketEventClient = new SpringWebSocketClient(RELAY_URI);
    String eventResponse = springWebSocketEventClient.send(eventMessage).stream().findFirst().get();

    ObjectMapper mapper = new ObjectMapper();
    assertTrue(
        JsonComparator.isEquivalentJson(
            mapper.readTree(expectedEventResponseJson(event.getId())),
            mapper.readTree(eventResponse)));

    springWebSocketEventClient.closeSocket();

    SpringWebSocketClient springWebSocketRequestClient = new SpringWebSocketClient(RELAY_URI);
    String reqJson = createReqJson(SUBSCRIBER_ID, eventId);
    String reqResponse = springWebSocketRequestClient.send(reqJson).stream().findFirst().get();

    assertTrue(
        JsonComparator.isEquivalentJson(
            mapper.readTree(expectedRequestResponseJson()),
            mapper.readTree(reqResponse)));

    springWebSocketRequestClient.closeSocket();
  }

  private String expectedEventResponseJson(String subscriptionId) {
    return "[\"OK\",\"" + subscriptionId + "\",true,\"success: request processed\"]";
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private String expectedRequestResponseJson() {
    return
        "   [\"EVENT\",\"" + SUBSCRIBER_ID + "\",\n" +
            "          {\"id\": \"" + eventId + "\",\n" +
            "          \"kind\": " + KIND + ",\n" +
            "          \"content\": \"" + CALENDAR_CONTENT + "\",\n" +
            "          \"pubkey\": \"" + eventPubKey + "\",\n" +
            "          \"created_at\": " + CREATED_AT + ",\n" +
            "          \"tags\": [\n" +
            "            [ \"e\", \"" + E_TAG.getIdEvent() + "\", \"" + E_TAG.getMarker() + "\" ],\n" +
            "            [ \"g\", \"" + G_TAG.getLocation() + "\" ],\n" +
            "            [ \"t\", \"" + T_TAG.getHashTag() + "\" ],\n" +
            "            [ \"d\", \"" + UUID_CALENDAR_TIME_BASED_EVENT_TEST + "\" ],\n" +
            "            [ \"p\", \"" + P1_TAG.getPublicKey() + "\", \"" + RELAY_URI + "\", \"" + P1_ROLE + "\" ],\n" +
            "            [ \"p\", \"" + P2_TAG.getPublicKey() + "\", \"" + RELAY_URI + "\", \"" + P2_ROLE + "\" ],\n" +
            "            [ \"start_tzid\", \"" + START_TZID + "\" ],\n" +
            "            [ \"end_tzid\", \"" + END_TZID + "\" ],\n" +
            "            [ \"summary\", \"" + SUMMARY + "\" ],\n" +
            "            [ \"l\", \"" + LABEL_1 + "\" ],\n" +
            "            [ \"l\", \"" + LABEL_2 + "\" ],\n" +
            "            [ \"location\", \"" + LOCATION + "\" ],\n" +
            "            [ \"r\", \"" + URI.create(RELAY_URI) + "\" ],\n" +
            "            [ \"title\", \"" + TITLE + "\" ],\n" +
            "            [ \"start\", \"" + START + "\" ],\n" +
            "            [ \"end\", \"" + END + "\" ]\n" +
            "          ],\n" +
            "          \"sig\": \"" + signature + "\"\n" +
            "        }]";
  }
}
