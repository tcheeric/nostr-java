package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP99Event;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
public class ClassifiedListingEvent extends NIP99Event {
  @Key
  @EqualsAndHashCode.Exclude
  private final String summary;
  @Key
  @EqualsAndHashCode.Exclude
  private final String location;
  @Key
  @EqualsAndHashCode.Exclude
  @JsonProperty("price")
  private final List<String> price;
  @Key
  @EqualsAndHashCode.Exclude
  private final String title;
  @Key
  @EqualsAndHashCode.Exclude
  private final String currency;

  public ClassifiedListingEvent(PublicKey pubKey, List<BaseTag> tags, String content, String title, String summary, String location, @NonNull List<String> price, String currency) {
    super(pubKey, Kind.CLASSIFIED_LISTING, tags, content);
    this.title = title;
    this.summary = summary;
    this.location = location;
    this.price = price;
    this.currency = currency;
  }
}
