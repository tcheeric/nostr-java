package nostr.test.filters;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.filter.AddressableTagFilter;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log
public class FiltersEncoderTest {

  @Test
  public void testEventFilterEncoder() {
    log.info("testEventFilterEncoder");

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();

    String filterKey = EventFilter.filterKey;
    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    expectedFilters.put(filterKey,
        List.of(
            new EventFilter<>(new GenericEvent(eventId))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"ids\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testKindFiltersEncoder() {
    log.info("testKindFiltersEncoder");

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    Kind kind = Kind.valueOf(1);

    String filterKey = KindFilter.filterKey;
    expectedFilters.put(filterKey,
        List.of(
            new KindFilter<>(kind)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"kinds\":[" + kind.toString() + "]}", encodedFilters);
  }

  @Test
  public void testAuthorFilterEncoder() {
    log.info("testAuthorFilterEncoder");

    String pubKeyString = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(AuthorFilter.filterKey,
        List.of(
            new AuthorFilter<>(new PublicKey(pubKeyString))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"authors\":[\"" + pubKeyString + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleAuthorFilterEncoder() {
    log.info("testMultipleAuthorFilterEncoder");

    String pubKeyString1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String pubKeyString2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(AuthorFilter.filterKey,
        List.of(
            new AuthorFilter<>(new PublicKey(pubKeyString1)),
            new AuthorFilter<>(new PublicKey(pubKeyString2))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String authorPubKeys = String.join("\",\"", pubKeyString1, pubKeyString2);

    assertEquals("{\"authors\":[\"" + authorPubKeys + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleKindFiltersEncoder() {
    log.info("testMultipleKindFiltersEncoder");

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    Kind kind1 = Kind.valueOf(1);
    Kind kind2 = Kind.valueOf(2);

    String filterKey = KindFilter.filterKey;
    expectedFilters.put(filterKey,
        List.of(
            new KindFilter<>(kind1),
            new KindFilter<>(kind2)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String kinds = String.join(",", kind1.toString(), kind2.toString());
    assertEquals("{\"kinds\":[" + kinds + "]}", encodedFilters);
  }

  @Test
  public void testAddressableTagFilterEncoder() {
    log.info("testAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidKey = "#d";
    String uuidValue1 = "UUID-1";

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(kind);
    addressTag.setPublicKey(new PublicKey(author));
    addressTag.setIdentifierTag(new IdentifierTag(uuidValue1));

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(uuidKey,
        List.of(
            new AddressableTagFilter<>(addressTag)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String addressableTag = String.join(":", String.valueOf(kind), author, uuidValue1);

    assertEquals("{\"#a\":[\"" + addressableTag + "\"]}", encodedFilters);
  }

  @Test
  public void testIdentifierTagFilterEncoder() {
    log.info("testIdentifierTagFilterEncoder");

    String uuidValue1 = "UUID-1";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(IdentifierTagFilter.filterKey,
        List.of(
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue1))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#d\":[\"" + uuidValue1 + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleIdentifierTagFilterEncoder() {
    log.info("testMultipleIdentifierTagFilterEncoder");

    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(IdentifierTagFilter.filterKey,
        List.of(
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue1)),
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue2))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String dTags = String.join("\",\"", uuidValue1, uuidValue2);
    assertEquals("{\"#d\":[\"" + dTags + "\"]}", encodedFilters);
  }

  @Test
  public void testReferencedEventFilterEncoder() {
    log.info("testReferencedEventFilterEncoder");

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(ReferencedEventFilter.filterKey,
        List.of(
            new ReferencedEventFilter<>(new GenericEvent(eventId))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#e\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleReferencedEventFilterEncoder() {
    log.info("testMultipleReferencedEventFilterEncoder");

    String eventId1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String eventId2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(ReferencedEventFilter.filterKey,
        List.of(
            new ReferencedEventFilter<>(new GenericEvent(eventId1)),
            new ReferencedEventFilter<>(new GenericEvent(eventId2))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String eventIds = String.join("\",\"", eventId1, eventId2);
    assertEquals("{\"#e\":[\"" + eventIds + "\"]}", encodedFilters);
  }

  @Test
  public void testSingleGenericTagQueryFiltersEncoder() {
    log.info("testSingleGenericTagQueryFiltersEncoder");

    String geohashKey = "#g";
    String new_geohash = "2vghde";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(geohashKey,
        List.of(
            new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, new_geohash))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#g\":[\"2vghde\"]}", encodedFilters);
  }

  @Test
  public void testReferencedPublicKeyFilterEncoder() {
    log.info("testReferencedPublicKeyFilterEncoder");

    String pubKeyString = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(ReferencedPublicKeyFilter.filterKey,
        List.of(
            new ReferencedPublicKeyFilter<>(new PublicKey(pubKeyString))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#p\":[\"" + pubKeyString + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleReferencedPublicKeyFilterEncoder() {
    log.info("testMultipleReferencedPublicKeyFilterEncoder");

    String pubKeyString1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String pubKeyString2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(ReferencedPublicKeyFilter.filterKey,
        List.of(
            new ReferencedPublicKeyFilter<>(new PublicKey(pubKeyString1)),
            new ReferencedPublicKeyFilter<>(new PublicKey(pubKeyString2))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    String pubKeyTags = String.join("\",\"", pubKeyString1, pubKeyString2);
    assertEquals("{\"#p\":[\"" + pubKeyTags + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleGenericTagQueryFiltersEncoder() {
    log.info("testMultipleGenericTagQueryFiltersEncoder");

    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(geohashKey,
        List.of(
            new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, geohashValue1)),
            new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, geohashValue2))));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#g\":[\"2vghde\",\"3abcde\"]}", encodedFilters);
  }

  @Test
  public void testMultipleAddressableTagFilterEncoder() {
    log.info("testMultipleAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidKey = "#a";
    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";

    String addressableTag1 = String.join(":", String.valueOf(kind), author, uuidValue1);
    String addressableTag2 = String.join(":", String.valueOf(kind), author, uuidValue2);

    AddressTag addressTag1 = new AddressTag();
    addressTag1.setKind(kind);
    addressTag1.setPublicKey(new PublicKey(author));
    addressTag1.setIdentifierTag(new IdentifierTag(uuidValue1));

    AddressTag addressTag2 = new AddressTag();
    addressTag2.setKind(kind);
    addressTag2.setPublicKey(new PublicKey(author));
    addressTag2.setIdentifierTag(new IdentifierTag(uuidValue2));

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(uuidKey,
        List.of(
            new AddressableTagFilter<>(addressTag1),
            new AddressableTagFilter<>(addressTag2)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encoded = encoder.encode();
    String addressableTags = String.join("\",\"", addressableTag1, addressableTag2);
    assertEquals("{\"#a\":[\"" + addressableTags + "\"]}", encoded);
  }

  @Test
  public void testSinceFiltersEncoder() {
    log.info("testSinceFiltersEncoder");

    String sinceKey = SinceFilter.filterKey;
    Long since = Date.from(Instant.now()).getTime();

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(sinceKey,
        List.of(
            new SinceFilter(since)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"since\":" + since + "}", encodedFilters);
  }

  @Test
  public void testUntilFiltersEncoder() {
    log.info("testUntilFiltersEncoder");

    String untilKey = UntilFilter.filterKey;
    Long until = Date.from(Instant.now()).getTime();

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(untilKey,
        List.of(
            new UntilFilter(until)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"until\":" + until + "}", encodedFilters);
  }
}
