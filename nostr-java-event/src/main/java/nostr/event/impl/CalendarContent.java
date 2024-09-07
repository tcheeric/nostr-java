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
  private final String id;

  private final IdentifierTag identifierTag;

  //@JsonProperty
  private final String title;

  //@JsonProperty
  private final Long start;

  // below fields optional
  //@JsonProperty
  private Long end;

  //@JsonProperty("start_tzid")
  private String startTzid;

  //@JsonProperty("end_tzid")
  private String endTzid;

  //@JsonProperty
  private String summary;

  //@JsonProperty
  private String image;

  //@JsonProperty
  private String location;

  private GeohashTag geohashTag;

  private List<PubKeyTag> participantPubKeys;

  //@JsonProperty("l")
  private List<String> labels;

  private List<HashtagTag> hashtagTags;

  private List<ReferenceTag> referenceTags;

  public static CalendarContentBuilder builder(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
    return new CalendarContentBuilder()
        .identifierTag(identifierTag)
        .title(title)
        .start(start);
  }
//  public CalendarContent(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
//    this.identifierTag = identifierTag;
//    this.title = title;
//    this.start = start;
//  }
//
//  public CalendarContent(@NonNull String uuid, @NonNull String title, @NonNull Long start) {
//    this.identifierTag = new IdentifierTag(uuid);
//    this.title = title;
//    this.start = start;
//  }
}