package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.entities.CalendarRsvpContent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.id.Identity;
import org.apache.commons.lang3.stream.Streams;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static nostr.api.NIP01.createIdentifierTag;
import static nostr.api.NIP23.createImageTag;
import static nostr.api.NIP23.createSummaryTag;
import static nostr.api.NIP23.createTitleTag;
import static nostr.api.NIP99.createLocationTag;
import static nostr.api.NIP99.createStatusTag;

/**
 * NIP-52 helpers (Calendar Events). Build time/date-based calendar events and RSVP.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/52.md">NIP-52</a>
 */
public class NIP52 extends EventNostr {
  public NIP52(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a time-based calendar event (kind 31922) with provided tags and content.
   *
   * @param baseTags additional tags to include (e.g., location, labels)
   * @param content optional human-readable content/notes
   * @param calendarContent the structured calendar content (identifier, title, start, etc.)
   * @return this instance for chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP52 createCalendarTimeBasedEvent(
      @NonNull List<BaseTag> baseTags,
      @NonNull String content,
      @NonNull CalendarContent<BaseTag> calendarContent) {

    GenericEvent genericEvent =
        new GenericEventFactory(
                getSender(), Kind.CALENDAR_TIME_BASED_EVENT.getValue(), baseTags, content)
            .create();

    genericEvent.addTag(calendarContent.getIdentifierTag());
    genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
    genericEvent.addTag(createStartTag(calendarContent.getStart()));

    Optional<GeohashTag> geohashTag = calendarContent.getGeohashTag();
    geohashTag.ifPresent(genericEvent::addTag);
    calendarContent.getEnd().ifPresent(aLong -> genericEvent.addTag(createEndTag(aLong)));
    calendarContent.getStartTzid().ifPresent(s -> genericEvent.addTag(createStartTzidTag(s)));
    calendarContent.getEndTzid().ifPresent(s -> genericEvent.addTag(createEndTzidTag(s)));
    calendarContent.getSummary().ifPresent(s -> genericEvent.addTag(createSummaryTag(s)));

    calendarContent
        .getImage()
        .ifPresent(
            s ->
                genericEvent.addTag(
                    createImageTag(
                        Streams.failableStream(URI.create(s)).map(URI::toURL).stream()
                            .findFirst()
                            .orElseThrow())));

    calendarContent.getParticipantPubKeyTags().forEach(genericEvent::addTag);
    calendarContent.getLocation().ifPresent(s -> genericEvent.addTag(createLocationTag(s)));
    calendarContent.getHashtagTags().forEach(genericEvent::addTag);
    calendarContent.getReferenceTags().forEach(genericEvent::addTag);
    calendarContent.getLabelTags().forEach(genericEvent::addTag);

    this.updateEvent(genericEvent);

    return this;
  }

  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP52 createCalendarRsvpEvent(
      @NonNull String content, @NonNull CalendarRsvpContent calendarRsvpContent) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Kind.CALENDAR_RSVP_EVENT.getValue(), content).create();

    //        mandatory tags
    genericEvent.addTag(calendarRsvpContent.getIdentifierTag());
    genericEvent.addTag(calendarRsvpContent.getAddressTag());
    genericEvent.addTag(createStatusTag(calendarRsvpContent.getStatus()));

    //        optional tags
    calendarRsvpContent.getAuthorPubKeyTag().ifPresent(genericEvent::addTag);
    calendarRsvpContent.getEventTag().ifPresent(genericEvent::addTag);
    calendarRsvpContent.getFbTag().ifPresent(genericEvent::addTag);

    this.updateEvent(genericEvent);

    return this;
  }

  /**
   * Create a date-based (all-day) calendar event using calendar content fields.
   *
   * @param content optional human-readable content/notes
   * @param calendarContent the structured calendar content (identifier, title, dates)
   * @return this instance for chaining
   */
  @SuppressWarnings({"rawtypes","unchecked"})
  public NIP52 createDateBasedCalendarEvent(
      @NonNull String content, @NonNull CalendarContent<BaseTag> calendarContent) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Kind.CALENDAR_DATE_BASED_EVENT.getValue(), content)
            .create();

    //        mandatory tags
    genericEvent.addTag(calendarContent.getIdentifierTag());
    genericEvent.addTag(createTitleTag(calendarContent.getTitle()));
    genericEvent.addTag(createStartTag(calendarContent.getStart()));

    //        optional tags
    calendarContent.getGeohashTag().ifPresent(genericEvent::addTag);
    calendarContent.getEnd().ifPresent(s -> genericEvent.addTag(createEndTag(s)));
    calendarContent.getStartTzid().ifPresent(s -> genericEvent.addTag(createStartTzidTag(s)));
    calendarContent.getEndTzid().ifPresent(s -> genericEvent.addTag(createEndTzidTag(s)));
    calendarContent.getSummary().ifPresent(s -> genericEvent.addTag(createSummaryTag(s)));

    this.updateEvent(genericEvent);
    return this;
  }

  public NIP52 addIdentifierTag(@NonNull String identifier) {
    addTag(createIdentifierTag(identifier));
    return this;
  }

  /**
   * Add a title tag to the current calendar event.
   *
   * @param title the event title
   * @return this instance for chaining
   */
  public NIP52 addTitleTag(@NonNull String title) {
    addTag(createTitleTag(title));
    return this;
  }

  /**
   * Add a start timestamp to the current calendar event.
   *
   * @param start unix timestamp (seconds)
   * @return this instance for chaining
   */
  public NIP52 addStartTag(@NonNull Long start) {
    addTag(createStartTag(start));
    return this;
  }

  /**
   * Add an end timestamp to the current calendar event.
   *
   * @param end unix timestamp (seconds)
   * @return this instance for chaining
   */
  public NIP52 addEndTag(@NonNull Long end) {
    addTag(createEndTag(end));
    return this;
  }

  public NIP52 addEventTag(@NonNull EventTag eventTag) {
    addTag(eventTag);
    return this;
  }

  /**
   * Create a {@code start} tag specifying the start timestamp.
   *
   * @param start unix timestamp (seconds)
   * @return the created tag
   */
  public static BaseTag createStartTag(@NonNull Long start) {
    return new BaseTagFactory(Constants.Tag.START_CODE, start.toString()).create();
  }

  /**
   * Create an {@code end} tag specifying the end timestamp.
   *
   * @param end unix timestamp (seconds)
   * @return the created tag
   */
  public static BaseTag createEndTag(@NonNull Long end) {
    return new BaseTagFactory(Constants.Tag.END_CODE, end.toString()).create();
  }

  /**
   * Create a {@code start_tzid} tag specifying timezone ID for start.
   *
   * @param startTzid IANA timezone identifier for the start
   * @return the created tag
   */
  public static BaseTag createStartTzidTag(@NonNull String startTzid) {
    return new BaseTagFactory(Constants.Tag.START_TZID_CODE, startTzid).create();
  }

  /**
   * Create an {@code end_tzid} tag specifying timezone ID for end.
   *
   * @param endTzid IANA timezone identifier for the end
   * @return the created tag
   */
  public static BaseTag createEndTzidTag(@NonNull String endTzid) {
    return new BaseTagFactory(Constants.Tag.END_TZID_CODE, endTzid).create();
  }

  /**
   * Create a {@code fb} (free-busy) tag describing availability.
   *
   * @param fb the free-busy value (e.g., free/busy/tentative)
   * @return the created tag
   */
  public static BaseTag createFreeBusyTag(@NonNull String fb) {
    return new BaseTagFactory(Constants.Tag.FREE_BUSY_CODE, fb).create();
  }
}
