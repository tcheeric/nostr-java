package nostr.test.event;

import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

class FiltersListTest {
  final GenericEvent event = new GenericEvent();
  final PublicKey publicKey = new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984");
  final PublicKey author = new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984");
  final GenericEvent referencedEvent = new GenericEvent();
  final PublicKey referencedPublicKey = new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984");
  final Long since = 1687765220L;
  final Long until = 1687765230L;
  final Integer limit = 1;
  final GenericTagQuery genericTagQuery = new GenericTagQuery();

  final List<GenericEvent> eventList = new ArrayList<>(List.of(event));
  final List<PublicKey> authorsList = new ArrayList<>(List.of(author));
  final List<PublicKey> publicKeyList = new ArrayList<>(List.of(publicKey));
  final List<Kind> kindList = new ArrayList<>(List.of(Kind.TEXT_NOTE));

  final List<GenericEvent> referencedEventsList = new ArrayList<>(List.of(referencedEvent));
  final List<PublicKey> referencePubKeysList = new ArrayList<>(List.of(referencedPublicKey));

  Filters filters;

  @BeforeEach
  void setup() {
    filters = new Filters();
    filters.setEvents(eventList);
    filters.setAuthors(authorsList);
    filters.setKinds(kindList);
    filters.setReferencedEvents(referencedEventsList);
    filters.setReferencePubKeys(referencePubKeysList);
    filters.setSince(since);
    filters.setUntil(until);
    filters.setLimit(limit);
    filters.setGenericTagQuery(genericTagQuery);
  }

//  @JsonDeserialize(using = CustomFiltersListDeserializer.class)

  /**
   * public FNostrList()
   * public FNostrList(@NonNull T item)
   * public FNostrList(@NonNull List<T> items)
   * public FNostrList(@NonNull Set<T> uniqueItems)
   * public boolean addExclusive(@NonNull T elt) throws IllegalArgumentException
   * public boolean addAllExclusive(@NonNull Set<T> elt) throws IllegalArgumentException
   * public List<T> getList()
   */


  /**
   * private EventList<GenericEvent> events;
   * private PublicKeyList<PublicKey> authors;
   *
   * private KindList kinds;
   *
   Filters filter = new Filters();
   filter.setKinds(new KindList(1));
   filtersList.add(filter);
   assertEquals(1, filtersList.size());


   Filters filter = new Filters();
   filter.setKinds(new KindList(1));
   filtersList.add(filter);
   assertEquals(1, filtersList.size());
   *
   * private EventList<GenericEvent> referencedEvents;
   * private PublicKeyList<PublicKey> referencePubKeys;
   * private Long since;
   * private Long until;
   * private Integer limit;
   * private GenericTagQuery genericTagQuery;
   */
}