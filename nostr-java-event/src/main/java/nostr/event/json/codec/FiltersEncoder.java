package nostr.event.json.codec;

import nostr.base.Encoder;
import nostr.event.filter.EventFilter;

public record FiltersEncoder(EventFilter filter) implements Encoder {

  @Override
  public String encode() {
    return filter.toJson();
  }
}
