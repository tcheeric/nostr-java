
package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import nostr.base.IEvent;
import nostr.event.tag.PriceTag;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ClassifiedListingEventMessage extends EventMessage {

  @JsonProperty
  private String summary;
  @JsonProperty
  private String location;
  @JsonProperty("price")
  private List<PriceTag> price;
  @JsonProperty
  private String title;
  @JsonProperty
  private String currency;

  public ClassifiedListingEventMessage(@NonNull IEvent event, String subscriptionId, String summary, String location, List<PriceTag> price, String title, String currency) {
    super(event, subscriptionId);
    this.summary = summary;
    this.location = location;
    this.price = price;
    this.title = title;
    this.currency = currency;
  }
}
