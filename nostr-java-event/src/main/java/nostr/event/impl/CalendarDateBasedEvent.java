package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarDateBasedEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

@Event(name = "Date-Based Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarDateBasedEventDeserializer.class)
@NoArgsConstructor
public class CalendarDateBasedEvent<T extends BaseTag>
    extends AbstractBaseCalendarEvent<CalendarContent<T>> {

  public CalendarDateBasedEvent(PublicKey sender, List<BaseTag> baseTags, String content) {
    super(sender, Kind.CALENDAR_DATE_BASED_EVENT, baseTags, content);
  }

  public String getId() {
    return getCalendarContent().getIdentifierTag().getUuid();
  }

  public String getTile() {
    return getCalendarContent().getTitle();
  }

  public Date getStart() {
    return new Date(getCalendarContent().getStart());
  }

  public Optional<Date> getEnd() {
    CalendarContent<T> calendarContent = getCalendarContent();
    Optional<Long> end = calendarContent.getEnd();
    return end.map(Date::new);
  }

  public Optional<String> getLocation() {
    return getCalendarContent().getLocation();
  }

  public Optional<String> getGeohash() {
    Optional<GeohashTag> geohashTag = getCalendarContent().getGeohashTag();
    return geohashTag.map(GeohashTag::getLocation);
  }

  public List<PubKeyTag> getParticipants() {
    return getCalendarContent().getParticipantPubKeyTags();
  }

  public List<HashtagTag> getHashtags() {
    return getCalendarContent().getHashtagTags();
  }

  public List<ReferenceTag> getReferences() {
    return getCalendarContent().getReferenceTags();
  }

  @Override
  protected CalendarContent<T> getCalendarContent() {
    CalendarContent<T> calendarContent =
        new CalendarContent<>(
            nostr.event.filter.Filterable.requireTagOfTypeWithCode(
                IdentifierTag.class, "d", this),
            nostr.event.filter.Filterable
                .requireTagOfTypeWithCode(GenericTag.class, "title", this)
                .getAttributes()
                .get(0)
                .value()
                .toString(),
            Long.parseLong(
                nostr.event.filter.Filterable
                    .requireTagOfTypeWithCode(GenericTag.class, "start", this)
                    .getAttributes()
                    .get(0)
                    .value()
                    .toString()));

    // Update the calendarContent object with the values from the tags
    nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "end", this)
        .ifPresent(
            tag ->
                calendarContent.setEnd(
                    Long.parseLong(tag.getAttributes().get(0).value().toString())));

    nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GenericTag.class, "location", this)
        .ifPresent(tag -> calendarContent.setLocation(tag.getAttributes().get(0).value().toString()));

    nostr.event.filter.Filterable
        .firstTagOfTypeWithCode(GeohashTag.class, "g", this)
        .ifPresent(calendarContent::setGeohashTag);

    nostr.event.filter.Filterable
        .getTypeSpecificTags(PubKeyTag.class, this)
        .forEach(calendarContent::addParticipantPubKeyTag);

    nostr.event.filter.Filterable
        .getTypeSpecificTags(HashtagTag.class, this)
        .forEach(calendarContent::addHashtagTag);

    nostr.event.filter.Filterable
        .getTypeSpecificTags(ReferenceTag.class, this)
        .forEach(calendarContent::addReferenceTag);

    return calendarContent;
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.CALENDAR_DATE_BASED_EVENT.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.CALENDAR_DATE_BASED_EVENT.getValue());
    }
  }
}
