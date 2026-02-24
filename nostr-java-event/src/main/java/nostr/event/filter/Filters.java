package nostr.event.filter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * Container for one or more EventFilter objects, used in REQ messages.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Filters {

  private final List<EventFilter> filters;

  public Filters(@NonNull EventFilter... filters) {
    this(List.of(filters));
  }

  public Filters(@NonNull List<EventFilter> filters) {
    if (filters.isEmpty()) {
      throw new IllegalArgumentException("Filters cannot be empty.");
    }
    this.filters = Collections.unmodifiableList(filters);
  }
}
