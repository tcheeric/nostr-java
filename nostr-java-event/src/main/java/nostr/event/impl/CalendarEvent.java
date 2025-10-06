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

    IdentifierTag idTag =
        nostr.event.filter.Filterable.requireTagOfTypeWithCode(IdentifierTag.class, "d", this);
    String title =
        nostr.event.filter.Filterable
            .requireTagOfTypeWithCode(GenericTag.class, "title", this)
            .getAttributes()
            .get(0)
            .value()
            .toString();

    CalendarContent<BaseTag> calendarContent = new CalendarContent<>(idTag, title, -1L);

    nostr.event.filter.Filterable
        .getTypeSpecificTags(AddressTag.class, this)
        .forEach(calendarContent::addAddressTag);

    return calendarContent;
  }

  @Override
  protected void validateTags() {
    super.validateTags();

    // Validate required tags ("d", "title")
    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(IdentifierTag.class, "d", this)
        .isEmpty()) {
      throw new AssertionError("Missing `d` tag for the event identifier.");
    }

    if (nostr.event.filter.Filterable.firstTagOfTypeWithCode(GenericTag.class, "title", this)
        .isEmpty()) {
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
