package nostr.test.event;

import nostr.event.Kind;
import nostr.event.impl.Filters;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventListTest {
  @Test
  void testConstructZapRequestEvent() {
    List<Filters> filtersList = new ArrayList<>();
    Filters filter = new Filters();
    filter.setKinds(new ArrayList<>(List.of(Kind.TEXT_NOTE)));
    filtersList.add(filter);
    assertEquals(1, filtersList.size());
  }
}