package nostr.test.filters;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.event.filter.AddressableTagFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log
public class FiltersEncoderTest {

  @Test
  public void testAddressableTagFilterEncoder() {
    log.info("testAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidKey = "#d";
    String uuidValue1 = "UUID-1";

    String joined = String.join(":", String.valueOf(kind), author, uuidValue1);

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(kind);
    addressTag.setPublicKey(new PublicKey(author));
    addressTag.setIdentifierTag(new IdentifierTag(uuidValue1));

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(uuidKey,
        List.of(
            new AddressableTagFilter<>(addressTag)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String jsonMessage = encoder.encode();
    assertEquals("{\"#a\":[\"" + joined + "\"]}", jsonMessage);
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
    String jsonMessage = encoder.encode();
    assertEquals("{\"#g\":[\"2vghde\"]}", jsonMessage);
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
    String jsonMessage = encoder.encode();
    assertEquals(
        "{\"#g\":[\"2vghde\",\"3abcde\"]}"
        , jsonMessage);
  }

  @Test
  public void testMultipleAddressableTagFilterEncoder() {
    log.info("testMultipleAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidKey = "#a";
    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";

    String joined1 = String.join(":", String.valueOf(kind), author, uuidValue1);
    String joined2 = String.join(":", String.valueOf(kind), author, uuidValue2);

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
    String jsonMessage = encoder.encode();
    String joinedTags = String.join("\",\"", joined1, joined2);
    assertEquals("{\"#a\":[\"" + joinedTags + "\"]}", jsonMessage);
  }

  @Test
  public void testSinceFiltersEncoder() {
    log.info("testSinceFiltersEncoder");

    String sinceKey = SinceFilter.filterKey;
    Long since = 1111111111L;

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(sinceKey,
        List.of(
            new SinceFilter(since)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String jsonMessage = encoder.encode();
    assertEquals("{\"" + sinceKey + "\":" + since + "}", jsonMessage);
  }
}
