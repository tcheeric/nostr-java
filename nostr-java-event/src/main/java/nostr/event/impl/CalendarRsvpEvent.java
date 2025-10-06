package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.CalendarRsvpContent;
import nostr.event.json.deserializer.CalendarRsvpEventDeserializer;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;

@EqualsAndHashCode(callSuper = false)
@Event(name = "CalendarRsvpEvent", nip = 52)
@JsonDeserialize(using = CalendarRsvpEventDeserializer.class)
@NoArgsConstructor
public class CalendarRsvpEvent extends AbstractBaseCalendarEvent<CalendarRsvpContent> {

  public enum Status {
    ACCEPTED("accepted"),
    TENTATIVE("tentative"),
    DECLINED("declined");

    private final String status;

    Status(String status) {
      this.status = status;
    }

    @JsonValue
    public String getStatus() {
      return status;
    }
  }

  public enum FB {
    FREE("free"),
    BUSY("busy");

    private final String value;

    FB(String fb) {
      this.value = fb;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  public CalendarRsvpEvent(
      @NonNull PublicKey sender, @NonNull List<BaseTag> baseTags, @NonNull String content) {
    super(sender, Kind.CALENDAR_RSVP_EVENT, baseTags, content);
  }

  public Status getStatus() {
    return Status.valueOf(getCalendarContent().getStatus().toUpperCase());
  }

  public Optional<FB> getFB() {
    return getCalendarContent()
        .getFbTag()
        .map(fbTag -> fbTag.getAttributes().get(0).value().toString().toUpperCase())
        .map(FB::valueOf);
  }

  public Optional<String> getEventId() {
    return getCalendarContent().getEventTag().map(EventTag::getIdEvent);
  }

  public String getId() {
    return getCalendarContent().getIdentifierTag().getUuid();
  }

  public Optional<PublicKey> getAuthor() {
    return getCalendarContent().getAuthorPubKeyTag().map(PubKeyTag::getPublicKey);
  }

  @Override
  protected CalendarRsvpContent getCalendarContent() {
    CalendarRsvpContent calendarRsvpContent =
        CalendarRsvpContent.builder(
                nostr.event.filter.Filterable.requireTagOfTypeWithCode(
                    IdentifierTag.class, "d", this),
                nostr.event.filter.Filterable.requireTagOfTypeWithCode(
                    AddressTag.class, "a", this),
                nostr.event.filter.Filterable
                    .requireTagOfTypeWithCode(GenericTag.class, "status", this)
                    .getAttributes()
                    .get(0)
                    .value()
                    .toString())
            .build();

    nostr.event.filter.Filterable
        .firstTagOfType(EventTag.class, this)
        .ifPresent(calendarRsvpContent::setEventTag);
    // FB tag is encoded as a generic tag with code 'fb'
    Optional.ofNullable(getTag("fb"))
        .ifPresent(baseTag -> calendarRsvpContent.setFbTag((GenericTag) baseTag));
    nostr.event.filter.Filterable
        .firstTagOfType(PubKeyTag.class, this)
        .ifPresent(calendarRsvpContent::setAuthorPubKeyTag);

    return calendarRsvpContent;
  }

  @Override
  public void validateKind() {
    if (getKind() != Kind.CALENDAR_RSVP_EVENT.getValue()) {
      throw new AssertionError(
          "Invalid kind value. Expected " + Kind.CALENDAR_RSVP_EVENT.getValue());
    }
  }
}
