package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.CalendarTimeBasedEvent.CalendarTimeBasedEventBuilder;
import nostr.event.impl.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalendarTimeBasedEventTest {
  // required fields
  public static final String ID = "0000000000000000000000000000000000000000000000000000000000000000";
  public static final PublicKey senderPubkey = new PublicKey(Identity.generateRandomIdentity().getPublicKey().toString());
  public static final String CALENDAR_TIME_BASED_EVENT_TITLE = "Calendar Time-Based Event title";
  public static final String CALENDAR_TIME_BASED_EVENT_CONTENT = "calendar Time-Based Event content";
  public static final IdentifierTag identifierTag = new IdentifierTag("UUID-CalendarTimeBasedEventTest");
  public static final Long START = 1716513986268L;

  // optional fields
  public static final String PTAG_1_HEX = "2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985";
  public static final PubKeyTag P_1_TAG = new PubKeyTag(new PublicKey(PTAG_1_HEX), null, "ISSUER");
  public static final String PTAG_2_HEX = "494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347";
  public static final PubKeyTag P_2_TAG = new PubKeyTag(new PublicKey(PTAG_2_HEX), null, "COUNTERPARTY");

  public static final String SUBJECT = "Calendar Time-Based Event Test Subject Tag";
  public static final SubjectTag SUBJECT_TAG = new SubjectTag(SUBJECT);
  public static final GeohashTag G_TAG = new GeohashTag("Calendar Time-Based Event Test Geohash Tag");
  public static final HashtagTag T_TAG = new HashtagTag("Calendar Time-Based Event Test Hashtag Tag");

  public static final String CALENDAR_TIME_BASED_EVENT_SUMMARY = "Calendar Time-Based Event summary";
  public static final String CALENDAR_TIME_BASED_EVENT_START_TZID = "1687765220";
  public static final String CALENDAR_TIME_BASED_EVENT_END_TZID = "1687765220";
  public static final String CALENDAR_TIME_BASED_EVENT_LOCATION = "Calendar Time-Based Event location";

  // keys
  public static final String START_TZID_CODE = "start_tzid";
  public static final String END_CODE = "end";
  public static final String LOCATION_CODE = "location";

  private CalendarTimeBasedEvent instance;
  private GenericTag startTzidGenericTag;

  @BeforeAll
  void setup() throws URISyntaxException {
    // a random set of base tags
    List<BaseTag> tags = new ArrayList<>();
    tags.add(P_1_TAG);
    tags.add(P_2_TAG);
    tags.add(GenericTag.create(LOCATION_CODE, 52, CALENDAR_TIME_BASED_EVENT_LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);
    startTzidGenericTag = GenericTag.create(START_TZID_CODE, 52, CALENDAR_TIME_BASED_EVENT_START_TZID);
    tags.add(startTzidGenericTag);
    Long l = START + 100L;
    tags.add(GenericTag.create(END_CODE, 52, l.toString()));

    CalendarContent calendarContent = CalendarContent.builder(identifierTag, CALENDAR_TIME_BASED_EVENT_TITLE, START).build();

    // a random set of calendar tags
    calendarContent.setEndTzid(CALENDAR_TIME_BASED_EVENT_END_TZID);
    calendarContent.setSummary(CALENDAR_TIME_BASED_EVENT_SUMMARY);
    calendarContent.setReferenceTags(List.of(new ReferenceTag(new URI("http://some.url"))));

    CalendarTimeBasedEventBuilder<?, ?> builder = CalendarTimeBasedEvent.builder();
    builder.pubKey(senderPubkey);
    builder.tags(tags);
    builder.kind(Kind.CALENDAR_TIME_BASED_EVENT.getValue());
    builder.content(CALENDAR_TIME_BASED_EVENT_CONTENT);
    builder.calendarContent(calendarContent);
    instance = builder.build();

    instance.setId(ID);
    instance.appendTags();
    instance.setSignature(Identity.generateRandomIdentity().sign(instance));
  }

  @Test
  void testCalendarTimeBasedEventEncoding() {
    System.out.println("testConstructCalendarTimeBasedEvent");
    assertEquals(14, instance.getTags().size());
    assertEquals(CALENDAR_TIME_BASED_EVENT_CONTENT, instance.getContent());
    Integer kind = instance.getKind();
    assertEquals(Kind.CALENDAR_TIME_BASED_EVENT.getValue(), kind.intValue());
    assertEquals(senderPubkey.toString(), instance.getPubKey().toString());
    assertEquals(senderPubkey.toBech32String(), instance.getPubKey().toBech32String());
    assertEquals(senderPubkey.toHexString(), instance.getPubKey().toHexString());
    assertEquals(CALENDAR_TIME_BASED_EVENT_CONTENT, instance.getContent());
    assertTrue(instance.getTags().contains(P_1_TAG));
    assertTrue(instance.getTags().contains(P_2_TAG));
    assertTrue(instance.getTags().contains(identifierTag));
    assertTrue(instance.getTags().contains(GenericTag.create("start", 52, START.toString())));
    assertTrue(instance.getTags().contains(startTzidGenericTag));
    assertTrue(instance.getTags().contains(GenericTag.create("title", 52, CALENDAR_TIME_BASED_EVENT_TITLE)));
  }

  @Test
  void testCalendarTimeBasedEventDecoding() throws JsonProcessingException {
    String json = "{\"id\":\"" + instance.getId() + "\",\"kind\":31923,\"content\":\"calendar Time-Based Event content\",\"pubkey\":\"" + senderPubkey + "\",\"created_at\":1725652600,\"tags\":[[\"p\",\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76985\",\"ISSUER\"],[\"p\",\"494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347\",\"COUNTERPARTY\"],[\"location\",\"Calendar Time-Based Event location\"],[\"subject\",\"Calendar Time-Based Event Test Subject Tag\"],[\"g\",\"Calendar Time-Based Event Test Geohash Tag\"],[\"t\",\"Calendar Time-Based Event Test Hashtag Tag\"],[\"start_tzid\",\"1687765220\"],[\"end\",\"1716513986368\"],[\"d\",\"UUID-CalendarTimeBasedEventTest\"],[\"title\",\"Calendar Time-Based Event title\"],[\"start\",\"1716513986268\"],[\"end_tzid\",\"1687765220\"],[\"summary\",\"Calendar Time-Based Event summary\"],[\"r\",\"http://some.url\"]],\"sig\":\"5021481421f32a1aadcaaa95c40abdd1a923b5c8ead086336a42ab28bb48084e35a0b1dd01a44f55daca535f4acd3d31319b506bb35eec530a8e9bac0add4f6a\"}";

    CalendarTimeBasedEvent event = new ObjectMapper().readValue(json, CalendarTimeBasedEvent.class);
    assertEquals(instance, event);
  }
}