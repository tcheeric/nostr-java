package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.entities.CalendarContent;
import nostr.event.json.deserializer.CalendarEventDeserializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;

import java.util.List;
import java.util.Optional;

@Event(name = "Calendar Event", nip = 52)
@JsonDeserialize(using = CalendarEventDeserializer.class)
@NoArgsConstructor
public class CalendarEvent extends AbstractBaseCalendarEvent {

    public CalendarEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
        super(sender, Kind.CALENDAR_EVENT, baseTags, content);
    }

    public String getId() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getIdentifierTag().getId();
    }

    public String getTitle() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getTitle();
    }

    public List<String> getCalendarEventIds() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getAddressTags().stream()
                .map(tag -> tag.getIdentifierTag().getId())
                .toList();
    }

    public List<PublicKey> getCalendarEventAuthors() {
        CalendarContent calendarContent = getCalendarContent();
        return calendarContent.getAddressTags().stream()
                .map(tag -> tag.getPublicKey())
                .toList();
    }

    @Override
    protected CalendarContent getCalendarContent() {

        GenericTag identifierTag = getTag("d");
        GenericTag titleTag = getTag("title");

        CalendarContent calendarContent = CalendarContent.builder(
                GenericTag.convert(identifierTag, IdentifierTag.class),
                titleTag.getAttributes().get(0).getValue().toString(),
                -1L).build();

        List<GenericTag> aTags = getTags("a");

        Optional.ofNullable(aTags).ifPresent(tags ->
                tags.forEach(aTag -> calendarContent.addAddressTag(GenericTag.convert(aTag, AddressTag.class)))
        );

        return calendarContent;
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate required tags ("d", "title")
        GenericTag dTag = getTag("d");
        if (dTag == null) {
            throw new AssertionError("Missing `d` tag for the event identifier.");
        }

        GenericTag titleTag = getTag("title");
        if (titleTag == null) {
            throw new AssertionError("Missing `title` tag for the event title.");
        }
    }
}
