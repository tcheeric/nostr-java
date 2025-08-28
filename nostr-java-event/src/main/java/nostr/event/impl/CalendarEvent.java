package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.JsonContent;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarEventDeserializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;

@Event(name = "Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarEventDeserializer.class)
@NoArgsConstructor
public class CalendarEvent extends AbstractBaseCalendarEvent<JsonContent> {

  public CalendarEvent(
      @NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
    super(sender, Kind.CALENDAR_EVENT, baseTags, content);
  }

  public String getId() {
    return getCalendarContent().getIdentifierTag().getUuid();
  }

  public String getTitle() {
    return getCalendarContent().getTitle();
  }

  public List<String> getCalendarEventIds() {
    return getCalendarContent().getAddressTags().stream()
        .map(tag -> tag.getIdentifierTag().getUuid())
        .toList();
  }

  public List<PublicKey> getCalendarEventAuthors() {
    return getCalendarContent().getAddressTags().stream().map(AddressTag::getPublicKey).toList();
  }

  @Override
  protected CalendarContent<BaseTag> getCalendarContent() {

    BaseTag identifierTag = getTag("d");
    BaseTag titleTag = getTag("title");

    CalendarContent<BaseTag> calendarContent =
        new CalendarContent<>(
            (IdentifierTag) identifierTag,
            ((GenericTag) titleTag).getAttributes().get(0).value().toString(),
            -1L);

    List<BaseTag> aTags = getTags("a");

    Optional.ofNullable(aTags)
        .ifPresent(tags -> tags.forEach(aTag -> calendarContent.addAddressTag((AddressTag) aTag)));

    return calendarContent;
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate required tags ("d", "title")
    BaseTag dTag = getTag("d");
    if (dTag == null) {
      throw new AssertionError("Missing `d` tag for the event identifier.");
    }

    BaseTag titleTag = getTag("title");
    if (titleTag == null) {
      throw new AssertionError("Missing `title` tag for the event title.");
    }
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.CALENDAR_EVENT.getValue()) {
      throw new AssertionError("Invalid kind value. Expected " + Kind.CALENDAR_EVENT.getValue());
    }
  }
}
