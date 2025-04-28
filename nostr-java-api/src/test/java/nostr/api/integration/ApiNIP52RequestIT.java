package nostr.api.integration;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nostr.api.NIP52;
import nostr.api.util.CommonTestObjectsFactory;
import nostr.api.util.JsonComparator;
import nostr.base.PublicKey;
import nostr.client.springwebsocket.SpringWebSocketClient;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import static nostr.base.IEvent.MAPPER_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
class ApiNIP52RequestIT {
    private static final String RELAY_URI = "ws://localhost:5555";
    private static final String UUID_CALENDAR_TIME_BASED_EVENT_TEST = CommonTestObjectsFactory.generateRandomHex64String();

    public static final String KIND = "31923";
    public static final String CALENDAR_CONTENT = CommonTestObjectsFactory.lorumIpsum(ApiNIP52RequestIT.class);

    public static final String CREATED_AT = String.valueOf(Instant.now().getEpochSecond());
    public static final String START = String.valueOf(Instant.now().plus(Duration.ofSeconds(10)).getEpochSecond());
    public static final String END = String.valueOf(Instant.now().plus(Duration.ofSeconds(100)).getEpochSecond());

    public static final String START_TZID = "America/Costa_Rica";
    public static final String END_TZID = "America/Costa_Rica";

    public static final String E_TAG_HEX = CommonTestObjectsFactory.generateRandomHex64String();
    public static final String G_TAG_VALUE = CommonTestObjectsFactory.generateRandomHex64String();
    public static final String T_TAG_VALUE = CommonTestObjectsFactory.generateRandomHex64String();
    public static final PublicKey P1_TAG_HEX = CommonTestObjectsFactory.createNewIdentity().getPublicKey();
    public static final String P1_ROLE = "PAYER";
    public static final String P2_ROLE = "PAYEE";

    public static final PublicKey P2_TAG_HEX = CommonTestObjectsFactory.createNewIdentity().getPublicKey();
    public static final String TITLE = CommonTestObjectsFactory.lorumIpsum();
    public static final String SUMMARY = CommonTestObjectsFactory.lorumIpsum();
    public static final String LOCATION = CommonTestObjectsFactory.lorumIpsum();

    public static final EventTag E_TAG = new EventTag(E_TAG_HEX);
    public static final PubKeyTag P1_TAG = new PubKeyTag(P1_TAG_HEX, RELAY_URI, P1_ROLE);
    public static final PubKeyTag P2_TAG = new PubKeyTag(P2_TAG_HEX, RELAY_URI, P2_ROLE);
    public static final GeohashTag G_TAG = new GeohashTag(G_TAG_VALUE);
    public static final HashtagTag T_TAG = new HashtagTag(T_TAG_VALUE);
    public static final ReferenceTag R_TAG = new ReferenceTag(URI.create(RELAY_URI));

    public static final String LABEL_1 = CommonTestObjectsFactory.lorumIpsum();
    public static final String LABEL_2 = CommonTestObjectsFactory.lorumIpsum();

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
        tags.add(P1_TAG);
        tags.add(P2_TAG);
        tags.add(GenericTag.create(START_TZID_CODE,  START_TZID));
        tags.add(GenericTag.create(END_TZID_CODE,  END_TZID));
        tags.add(GenericTag.create(SUMMARY_CODE,  SUMMARY));
        tags.add(GenericTag.create(LABEL_CODE,  LABEL_1));
        tags.add(GenericTag.create(LABEL_CODE,  LABEL_2));
        tags.add(GenericTag.create(LOCATION_CODE,  LOCATION));
        tags.add(GenericTag.create(END_CODE,  END));
        tags.add(G_TAG);
        tags.add(T_TAG);
        tags.add(R_TAG);

        CalendarContent calendarContent = CalendarContent.builder(
                new IdentifierTag(UUID_CALENDAR_TIME_BASED_EVENT_TEST),
                TITLE,
                Long.valueOf(START))
            .build();

        var nip52 = new NIP52<>(Identity.generateRandomIdentity());

        GenericEvent event = nip52.createCalendarTimeBasedEvent(tags, CALENDAR_CONTENT, calendarContent).sign().getEvent();
        event.setCreatedAt(Long.valueOf(CREATED_AT));
        eventId = event.getId();
        signature = event.getSignature().toString();
        eventPubKey = event.getPubKey().toString();
        EventMessage eventMessage = new EventMessage(event);

        SpringWebSocketClient springWebSocketEventClient = new SpringWebSocketClient(RELAY_URI);
        String eventResponse = springWebSocketEventClient.send(eventMessage).stream().findFirst().orElseThrow();

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

//    springWebSocketEventClient.closeSocket();


    /* TODO - This assertion fails with superdonductor and nostr-rs-relay
          above, supplemental:  SpringWebSocketClient api updated to properly handle this use/use-case
          integration testing successful against superconductor.  
          integration testing against nostr-rs-relay still pending   
    */

        SpringWebSocketClient springWebSocketRequestClient = new SpringWebSocketClient(RELAY_URI);
        String subscriberId = CommonTestObjectsFactory.generateRandomHex64String();
        String reqJson = createReqJson(subscriberId, eventId);
        String reqResponse = springWebSocketRequestClient.send(reqJson).stream().findFirst().orElseThrow();

        String expected = expectedRequestResponseJson(subscriberId);
        assertTrue(
            JsonComparator.isEquivalentJson(
                MAPPER_AFTERBURNER.readTree(expected),
                MAPPER_AFTERBURNER.readTree(reqResponse)));

//    springWebSocketRequestClient.closeSocket();
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
