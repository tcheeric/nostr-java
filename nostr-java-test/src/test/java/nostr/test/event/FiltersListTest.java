package nostr.test.event;

import nostr.event.impl.Filters;
import nostr.event.list.FiltersList;
import nostr.event.list.KindList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltersListTest {
  @Test
  void testConstructZapRequestEvent() {
    FiltersList filtersList = new FiltersList();
    Filters filter = new Filters();
    filter.setKinds(new KindList(1));
    filtersList.add(filter);
    assertEquals(1, filtersList.size());
  }
}