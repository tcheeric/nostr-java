package nostr.test.filters;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.json.codec.FiltersDecoder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log
public class FiltersDecoderTest {
  @Test
  public void testGenericTagFiltersDecoder() {
    log.info("testGenericTagFiltersDecoder");

    String geohashKey = "#g";
    String geohashValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue + "\"]}";

    Filters decodedFilters = new FiltersDecoder<>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(geohashKey, List.of(new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, geohashValue))));

    assertEquals(new Filters(expectedFilters), decodedFilters);
  }

  @Test
  public void testGenericTagFiltersListDecoder() {
    log.info("testGenericTagFiltersListDecoder");

    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]}";

    Filters decodedFilters = new FiltersDecoder<>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    Map<String, List<Filterable>> expectedFilters = new HashMap<>();
    expectedFilters.put(geohashKey, List.of(
        new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, geohashValue1)),
        new GenericTagQueryFilter<>(new GenericTagQuery(geohashKey, geohashValue2))));

    assertEquals(new Filters(expectedFilters), decodedFilters);
  }

}
