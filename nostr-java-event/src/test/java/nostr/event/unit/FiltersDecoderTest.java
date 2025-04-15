package nostr.event.unit;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.Kind;
import nostr.event.filter.AddressTagFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log
public class FiltersDecoderTest {

  @Test
  public void testEventFiltersDecoder() {
    log.info("testEventFiltersDecoder");

    String filterKey = "ids";
    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    String expected = "{\"" + filterKey + "\":[\"" + eventId + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new EventFilter<>(new GenericEvent(eventId))),
        decodedFilters);
  }

  @Test
  public void testMultipleEventFiltersDecoder() {
    log.info("testMultipleEventFiltersDecoder");

    String filterKey = "ids";
    String eventId1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String eventId2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    String joined = String.join("\",\"", eventId1, eventId2);

    String expected = "{\"" + filterKey + "\":[\"" + joined + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new EventFilter<>(new GenericEvent(eventId1)),
            new EventFilter<>(new GenericEvent(eventId2))),
        decodedFilters);
  }

  @Test
  public void testAddressableTagFiltersWithoutRelayDecoder() {
    log.info("testAddressableTagFiltersWithoutRelayDecoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidValue1 = "UUID-1";

    String joined = String.join(":", String.valueOf(kind), author, uuidValue1);

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(kind);
    addressTag.setPublicKey(new PublicKey(author));
    addressTag.setIdentifierTag(new IdentifierTag(uuidValue1));

    String expected = "{\"#a\":[\"" + joined + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new AddressTagFilter<>(addressTag)),
        decodedFilters);
  }

  @Test
  public void testAddressableTagFiltersWithRelayDecoder() {
    log.info("testAddressableTagFiltersWithRelayDecoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidValue1 = "UUID-1";
    Relay relay = new Relay("ws://localhost:5555");

    String joined = String.join(":", String.valueOf(kind), author, uuidValue1);

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(kind);
    addressTag.setPublicKey(new PublicKey(author));
    addressTag.setIdentifierTag(new IdentifierTag(uuidValue1));
    addressTag.setRelay(relay);

    String expected = String.join("\\\",\\\"", joined, relay.getUri());
    String addressableTag = "{\"#a\":[\"" + expected + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(addressableTag);

    Filters expected1 = new Filters(new AddressTagFilter<>(addressTag));
    assertEquals(expected1, decodedFilters);
  }

  @Test
  public void testMultipleAddressableTagFiltersDecoder() {
    log.info("testMultipleAddressableTagFiltersDecoder");

    Integer kind1 = 1;
    String author1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidValue1 = "UUID-1";

    Integer kind2 = 1;
    String author2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    String uuidValue2 = "UUID-2";

    AddressTag addressTag1 = new AddressTag();
    addressTag1.setKind(kind1);
    addressTag1.setPublicKey(new PublicKey(author1));
    addressTag1.setIdentifierTag(new IdentifierTag(uuidValue1));

    AddressTag addressTag2 = new AddressTag();
    addressTag2.setKind(kind2);
    addressTag2.setPublicKey(new PublicKey(author2));
    addressTag2.setIdentifierTag(new IdentifierTag(uuidValue2));

    String joined1 = String.join(":", String.valueOf(kind1), author1, uuidValue1);
    String joined2 = String.join(":", String.valueOf(kind2), author2, uuidValue2);

    String joined3 = String.join("\",\"", joined1, joined2);

    String expected = "{\"#a\":[\"" + joined3 + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new AddressTagFilter<>(addressTag1),
            new AddressTagFilter<>(addressTag2)),
        decodedFilters);
  }

  @Test
  public void testKindFiltersDecoder() {
    log.info("testKindFiltersDecoder");

    String filterKey = KindFilter.FILTER_KEY;
    Kind kind = Kind.valueOf(1);

    String expected = "{\"" + filterKey + "\":[" + kind.toString() + "]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(new Filters(new KindFilter<>(kind)), decodedFilters);
  }

  @Test
  public void testMultipleKindFiltersDecoder() {
    log.info("testMultipleKindFiltersDecoder");

    String filterKey = KindFilter.FILTER_KEY;
    Kind kind1 = Kind.valueOf(1);
    Kind kind2 = Kind.valueOf(2);

    String join = String.join(",", kind1.toString(), kind2.toString());

    String expected = "{\"" + filterKey + "\":[" + join + "]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new KindFilter<>(kind1),
            new KindFilter<>(kind2)),
        decodedFilters);
  }

  @Test
  public void testIdentifierTagFilterDecoder() {
    log.info("testIdentifierTagFilterDecoder");

    String uuidValue1 = "UUID-1";

    String expected = "{\"#d\":[\"" + uuidValue1 + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);


    assertEquals(new Filters(new IdentifierTagFilter<>(new IdentifierTag(uuidValue1))), decodedFilters);
  }

  @Test
  public void testMultipleIdentifierTagFilterDecoder() {
    log.info("testMultipleIdentifierTagFilterDecoder");

    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";

    String joined = String.join("\",\"", uuidValue1, uuidValue2);
    String expected = "{\"#d\":[\"" + joined + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue1)),
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue2))),
        decodedFilters);
  }

  @Test
  public void testReferencedEventFilterDecoder() {
    log.info("testReferencedEventFilterDecoder");

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    String expected = "{\"#e\":[\"" + eventId + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(new Filters(new ReferencedEventFilter<>(new EventTag(eventId))), decodedFilters);
  }

  @Test
  public void testMultipleReferencedEventFilterDecoder() {
    log.info("testMultipleReferencedEventFilterDecoder");

    String eventId1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String eventId2 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    String joined = String.join("\",\"", eventId1, eventId2);
    String expected = "{\"#e\":[\"" + joined + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new ReferencedEventFilter<>(new EventTag(eventId1)),
            new ReferencedEventFilter<>(new EventTag(eventId2))),
        decodedFilters);
  }

  @Test
  public void testReferencedPublicKeyFilterDecofder() {
    log.info("testReferencedPublicKeyFilterDecoder");

    String pubkeyString = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    String expected = "{\"#p\":[\"" + pubkeyString + "\"]}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(new Filters(new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubkeyString)))), decodedFilters);
  }

  @Test
  public void testMultipleReferencedPublicKeyFilterDecoder() {
    log.info("testMultipleReferencedPublicKeyFilterDecoder");

    String pubkeyString1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String pubkeyString2 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    String joined = String.join("\",\"", pubkeyString1, pubkeyString2);
    String expected = "{\"#p\":[\"" + joined + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(
        new Filters(
            new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubkeyString1))),
            new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubkeyString2)))),
        decodedFilters);
  }

  @Test
  public void testGeohashTagFiltersDecoder() {
    log.info("testGeohashTagFiltersDecoder");

    String geohashKey = "#g";
    String geohashValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(new Filters(new GeohashTagFilter<>(new GeohashTag(geohashValue))), decodedFilters);
  }

  @Test
  public void testMultipleGeohashTagFiltersDecoder() {
    log.info("testMultipleGeohashTagFiltersDecoder");

    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(new Filters(
            new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
            new GeohashTagFilter<>(new GeohashTag(geohashValue2))),
        decodedFilters);
  }

  @Test
  public void testHashtagTagFiltersDecoder() {
    log.info("testHashtagTagFiltersDecoder");

    String hashtagKey = "#t";
    String hashtagValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + hashtagKey + "\":[\"" + hashtagValue + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(new Filters(new HashtagTagFilter<>(new HashtagTag(hashtagValue))), decodedFilters);
  }

  @Test
  public void testMultipleHashtagTagFiltersDecoder() {
    log.info("testMultipleHashtagTagFiltersDecoder");

    String hashtagKey = "#t";
    String hashtagValue1 = "2vghde";
    String hashtagValue2 = "3abcde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + hashtagKey + "\":[\"" + hashtagValue1 + "\",\"" + hashtagValue2 + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(new Filters(
            new HashtagTagFilter<>(new HashtagTag(hashtagValue1)),
            new HashtagTagFilter<>(new HashtagTag(hashtagValue2))),
        decodedFilters);
  }

  @Test
  public void testGenericTagFiltersDecoder() {
    log.info("testGenericTagFiltersDecoder");

    String customTagKey = "#b";
    String customTagValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + customTagKey + "\":[\"" + customTagValue + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(new Filters(new GenericTagQueryFilter<>(new GenericTagQuery(customTagKey, customTagValue))), decodedFilters);
  }

  @Test
  public void testMultipleGenericTagFiltersDecoder() {
    log.info("testMultipleGenericTagFiltersDecoder");

    String customTagKey = "#b";
    String customTagValue1 = "2vghde";
    String customTagValue2 = "3abcde";

    String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + customTagKey + "\":[\"" + customTagValue1 + "\",\"" + customTagValue2 + "\"]}";

    Filters decodedFilters = new FiltersDecoder().decode(reqJsonWithCustomTagQueryFilterToDecode);

    assertEquals(
        new Filters(
            new GenericTagQueryFilter<>(new GenericTagQuery(customTagKey, customTagValue1)),
            new GenericTagQueryFilter<>(new GenericTagQuery(customTagKey, customTagValue2))),
        decodedFilters);
  }

  @Test
  public void testSinceFiltersDecoder() {
    log.info("testSinceFiltersDecoder");

    Long since = Date.from(Instant.now()).getTime();

    String expected = "{\"since\":" + since + "}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(new Filters(new SinceFilter(since)), decodedFilters);
  }

  @Test
  public void testUntilFiltersDecoder() {
    log.info("testUntilFiltersDecoder");

    Long until = Date.from(Instant.now()).getTime();

    String expected = "{\"until\":" + until + "}";
    Filters decodedFilters = new FiltersDecoder().decode(expected);

    assertEquals(new Filters(new UntilFilter(until)), decodedFilters);
  }

  @Test
  public void testFailedAddressableTagMalformedSeparator() {
    log.info("testFailedAddressableTagMalformedSeparator");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidValue1 = "UUID-1";

    String malformedJoin = String.join(",", String.valueOf(kind), author, uuidValue1);
    String expected = "{\"#a\":[\"" + malformedJoin + "\"]}";

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> new FiltersDecoder().decode(expected));
  }
}
