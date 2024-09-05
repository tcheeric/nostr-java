package nostr.test.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.GenericTag;
import nostr.event.message.EventMessage;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CalendarTimeBasedEventTest {
  // required fields
  public static final PublicKey senderPubkey = new PublicKey(Identity.generateRandomIdentity().getPublicKey().toString());
  public static final String CALENDAR_TIME_BASED_EVENT_TITLE = "Calendar Time-Based Event title";
  public static final String CALENDAR_TIME_BASED_EVENT_CONTENT = "calendar listing content";
  public static final IdentifierTag identifierTag = new IdentifierTag("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUID-CalendarTimeBasedEventTest");
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

  public static final String CALENDAR_TIME_BASED_EVENT_SUMMARY = "Calendar Time-Based Event listing summary";
  public static final String CALENDAR_TIME_BASED_EVENT_START_TZID = "1687765220";
  public static final String CALENDAR_TIME_BASED_EVENT_END_TZID = "1687765220";
  public static final String CALENDAR_TIME_BASED_EVENT_LOCATION = "Calendar Time-Based Event location";

  // keys
  public static final String START_TZID_CODE = "start_tzid";
  public static final String SUMMARY_CODE = "summary";
  public static final String END_CODE = "end";
  public static final String LOCATION_CODE = "location";

  private CalendarTimeBasedEvent instance;
  private GenericTag startTzidGenericTag;

  @BeforeAll
  void setup() {
    // a random set of base tags
    List<BaseTag> tags = new ArrayList<>();
    tags.add(P_1_TAG);
    tags.add(P_2_TAG);
    tags.add(GenericTag.create(SUMMARY_CODE, 52, CALENDAR_TIME_BASED_EVENT_SUMMARY));
    tags.add(GenericTag.create(LOCATION_CODE, 52, CALENDAR_TIME_BASED_EVENT_LOCATION));
    tags.add(SUBJECT_TAG);
    tags.add(G_TAG);
    tags.add(T_TAG);
    startTzidGenericTag = GenericTag.create(START_TZID_CODE, 52, CALENDAR_TIME_BASED_EVENT_START_TZID);
    tags.add(startTzidGenericTag);
    Long l = START + 100L;
    tags.add(GenericTag.create(END_CODE, 52, l.toString()));

    CalendarContent calendarContent = new CalendarContent(identifierTag, CALENDAR_TIME_BASED_EVENT_TITLE, START);
    // a random set of calendar tags
    calendarContent.setEndTzid(CALENDAR_TIME_BASED_EVENT_END_TZID);

    instance = new CalendarTimeBasedEvent(senderPubkey, tags, CALENDAR_TIME_BASED_EVENT_CONTENT, calendarContent);
    instance.setSignature(Identity.generateRandomIdentity().sign(instance));
  }

  @Test
  void testConstructCalendarTimeBasedEvent() throws JsonProcessingException {
    System.out.println("testConstructCalendarTimeBasedEvent");
    assertEquals(13, instance.getTags().size());
    assertEquals(CALENDAR_TIME_BASED_EVENT_CONTENT, instance.getContent());
    assertEquals(Kind.CALENDAR_TIME_BASED_EVENT.getValue(), instance.getKind().intValue());
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
}