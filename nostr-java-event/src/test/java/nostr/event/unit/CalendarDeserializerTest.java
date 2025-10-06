package nostr.event.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.json.EventJsonMapper;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarDateBasedEvent;
import nostr.event.impl.CalendarEvent;
import nostr.event.impl.CalendarRsvpEvent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.SubjectTag;
import org.junit.jupiter.api.Test;

class CalendarDeserializerTest {

  private static final PublicKey AUTHOR =
      new PublicKey("0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20");
  private static final String EVENT_ID =
      "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
  private static final Signature SIGNATURE =
      Signature.fromString("c".repeat(128));

  private GenericEvent baseEvent(int kind, List<BaseTag> tags) {
    return GenericEvent.builder()
        .id(EVENT_ID)
        .pubKey(AUTHOR)
        .customKind(kind)
        .tags(tags)
        .content("calendar payload")
        .createdAt(1_700_000_111L)
        .signature(SIGNATURE)
        .build();
  }

  private static BaseTag identifier(String value) {
    return IdentifierTag.builder().uuid(value).build();
  }

  private static BaseTag generic(String code, String value) {
    return BaseTag.create(code, value);
  }

  // Verifies the calendar event deserializer reconstructs identifier and title tags correctly.
  @Test
  void shouldDeserializeCalendarEvent() throws JsonProcessingException {
    AddressTag addressTag =
        AddressTag.builder()
            .kind(Kind.CALENDAR_EVENT.getValue())
            .publicKey(AUTHOR)
            .identifierTag(new IdentifierTag("event-123"))
            .build();

    GenericEvent genericEvent =
        baseEvent(
            Kind.CALENDAR_EVENT.getValue(),
            List.of(
                identifier("root-calendar"),
                generic("title", "Team calendar"),
                generic("start", "1700000100"),
                addressTag,
                new SubjectTag("planning")));

    String json = EventJsonMapper.mapper().writeValueAsString(genericEvent);
    CalendarEvent calendarEvent = EventJsonMapper.mapper().readValue(json, CalendarEvent.class);

    assertEquals("root-calendar", calendarEvent.getId());
    assertEquals("Team calendar", calendarEvent.getTitle());
    assertTrue(calendarEvent.getCalendarEventIds().contains("event-123"));
  }

  // Verifies date-based events expose optional metadata after round-trip deserialization.
  @Test
  void shouldDeserializeCalendarDateBasedEvent() throws JsonProcessingException {
    GenericEvent genericEvent =
        baseEvent(
            Kind.CALENDAR_DATE_BASED_EVENT.getValue(),
            List.of(
                identifier("date-calendar"),
                generic("title", "Date event"),
                generic("start", "1700000200"),
                generic("end", "1700000300"),
                generic("location", "Room 101"),
                new ReferenceTag(java.net.URI.create("https://relay.example"))));

    String json = EventJsonMapper.mapper().writeValueAsString(genericEvent);
    CalendarDateBasedEvent calendarEvent =
        EventJsonMapper.mapper().readValue(json, CalendarDateBasedEvent.class);

    assertEquals("date-calendar", calendarEvent.getId());
    assertEquals("Room 101", calendarEvent.getLocation().orElse(""));
    assertTrue(calendarEvent.getReferences().stream().anyMatch(tag -> tag.getUrl().isPresent()));
  }

  // Verifies time-based events deserialize timezone and summary tags.
  @Test
  void shouldDeserializeCalendarTimeBasedEvent() throws JsonProcessingException {
    GenericEvent genericEvent =
        baseEvent(
            Kind.CALENDAR_TIME_BASED_EVENT.getValue(),
            List.of(
                identifier("time-calendar"),
                generic("title", "Time event"),
                generic("start", "1700000400"),
                generic("start_tzid", "Europe/Amsterdam"),
                generic("end_tzid", "Europe/Amsterdam"),
                generic("summary", "Sync"),
                generic("location", "HQ")));

    String json = EventJsonMapper.mapper().writeValueAsString(genericEvent);
    CalendarTimeBasedEvent calendarEvent =
        EventJsonMapper.mapper().readValue(json, CalendarTimeBasedEvent.class);

    assertEquals("Europe/Amsterdam", calendarEvent.getStartTzid().orElse(""));
    assertEquals("Sync", calendarEvent.getSummary().orElse(""));
  }

  // Verifies RSVP events deserialize status, address, and optional event references.
  @Test
  void shouldDeserializeCalendarRsvpEvent() throws JsonProcessingException {
    AddressTag addressTag =
        AddressTag.builder()
            .kind(Kind.CALENDAR_EVENT.getValue())
            .publicKey(AUTHOR)
            .identifierTag(new IdentifierTag("calendar"))
            .build();

    GenericEvent genericEvent =
        baseEvent(
            Kind.CALENDAR_RSVP_EVENT.getValue(),
            List.of(
                identifier("rsvp-id"),
                addressTag,
                generic("status", "accepted"),
                new EventTag(EVENT_ID),
                new PubKeyTag(AUTHOR),
                generic("fb", "free")));

    String json = EventJsonMapper.mapper().writeValueAsString(genericEvent);
    CalendarRsvpEvent calendarEvent =
        EventJsonMapper.mapper().readValue(json, CalendarRsvpEvent.class);

    assertEquals(CalendarRsvpEvent.Status.ACCEPTED, calendarEvent.getStatus());
    assertEquals(EVENT_ID, calendarEvent.getEventId().orElse(""));
    assertTrue(calendarEvent.getFB().isPresent());
  }
}
