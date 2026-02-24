package nostr.event.unit;

import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FiltersTest {

  @Test
  void emptyFiltersListThrows() {
    assertThrows(IllegalArgumentException.class, () -> new Filters(new java.util.ArrayList<>()));
  }

  @Test
  void singleFilterWrapped() {
    EventFilter filter = EventFilter.builder().kind(1).build();
    Filters filters = new Filters(filter);
    assertEquals(1, filters.getFilters().size());
  }

  @Test
  void eventFilterBuilderBasic() {
    EventFilter filter = EventFilter.builder()
        .kind(1)
        .kind(7)
        .author("abc123")
        .since(1000L)
        .until(2000L)
        .limit(10)
        .addTagFilter("e", "eventid1")
        .build();

    assertEquals(java.util.List.of(1, 7), filter.getKinds());
    assertEquals(java.util.List.of("abc123"), filter.getAuthors());
    assertEquals(1000L, filter.getSince());
    assertEquals(2000L, filter.getUntil());
    assertEquals(10, filter.getLimit());
    assertEquals(java.util.List.of("eventid1"), filter.getTagFilters().get("e"));
  }
}
