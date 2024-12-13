package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.AbstractEventContent;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

import java.util.List;

@Data
@Builder
@JsonDeserialize(builder = CalendarContent.CalendarContentBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class CalendarContent extends AbstractEventContent<CalendarTimeBasedEvent> {
  //@JsonProperty
  private String id;

  // below fields mandatory
  private final IdentifierTag identifierTag;
  private final String title;
  private final Long start;

  // below fields optional
  private Long end;
  private String startTzid;
  private String endTzid;
  private String summary;
  private String image;
  private String location;
  private GeohashTag geohashTag;
  private List<PubKeyTag> participantPubKeys;
  private List<String> labels;
  private List<HashtagTag> hashtagTags;
  private List<ReferenceTag> referenceTags;

  public static CalendarContentBuilder builder(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
    return new CalendarContentBuilder()
        .identifierTag(identifierTag)
        .title(title)
        .start(start);
  }
}
