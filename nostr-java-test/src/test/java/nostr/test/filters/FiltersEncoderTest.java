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
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.SinceFilter;
import nostr.event.filter.UntilFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.message.ReqMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log
public class FiltersEncoderTest {

  @Test
  public void testEventFilterEncoderUsingVarArgs() {
    log.info("testEventFilterEncoderUsingVarArgs");

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new EventFilter<>(new GenericEvent(eventId))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"ids\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testEventFilterEncoderUsingList() {
    log.info("testEventFilterEncoderUsingList");

    List<Filterable> expectedFilters = new ArrayList<>();

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    expectedFilters.add(new EventFilter<>(new GenericEvent(eventId)));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(expectedFilters));
    String encodedFilters = encoder.encode();
    assertEquals("{\"ids\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testEventFilterEncoderByMap() {
    log.info("testEventFilterEncoderByMap");

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new EventFilter<>(new GenericEvent(eventId))));
    String encodedFilters = encoder.encode();
    assertEquals("{\"ids\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testKindFiltersEncoder() {
    log.info("testKindFiltersEncoder");

    Kind kind = Kind.valueOf(1);
    FiltersEncoder encoder = new FiltersEncoder(new Filters(new KindFilter<>(kind)));

    String encodedFilters = encoder.encode();
    assertEquals("{\"kinds\":[" + kind.toString() + "]}", encodedFilters);
  }

  @Test
  public void testAuthorFilterEncoder() {
    log.info("testAuthorFilterEncoder");

    String pubKeyString = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    FiltersEncoder encoder = new FiltersEncoder(new Filters(new AuthorFilter<>(new PublicKey(pubKeyString))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"authors\":[\"" + pubKeyString + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleAuthorFilterEncoder() {
    log.info("testMultipleAuthorFilterEncoder");

    String pubKeyString1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String pubKeyString2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        List.of(
            new AuthorFilter<>(new PublicKey(pubKeyString1)),
            new AuthorFilter<>(new PublicKey(pubKeyString2)))));

    String encodedFilters = encoder.encode();
    String authorPubKeys = String.join("\",\"", pubKeyString1, pubKeyString2);

    assertEquals("{\"authors\":[\"" + authorPubKeys + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleKindFiltersEncoder() {
    log.info("testMultipleKindFiltersEncoder");

    Kind kind1 = Kind.valueOf(1);
    Kind kind2 = Kind.valueOf(2);

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        List.of(
            new KindFilter<>(kind1),
            new KindFilter<>(kind2))));

    String encodedFilters = encoder.encode();
    String kinds = String.join(",", kind1.toString(), kind2.toString());
    assertEquals("{\"kinds\":[" + kinds + "]}", encodedFilters);
  }

  @Test
  public void testAddressableTagFilterEncoder() {
    log.info("testAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidValue1 = "UUID-1";

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(kind);
    addressTag.setPublicKey(new PublicKey(author));
    addressTag.setIdentifierTag(new IdentifierTag(uuidValue1));

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new AddressableTagFilter<>(addressTag)));
    String encodedFilters = encoder.encode();
    String addressableTag = String.join(":", String.valueOf(kind), author, uuidValue1);

    assertEquals("{\"#a\":[\"" + addressableTag + "\"]}", encodedFilters);
  }

  @Test
  public void testIdentifierTagFilterEncoder() {
    log.info("testIdentifierTagFilterEncoder");

    String uuidValue1 = "UUID-1";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new IdentifierTagFilter<>(new IdentifierTag(uuidValue1))));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#d\":[\"" + uuidValue1 + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleIdentifierTagFilterEncoder() {
    log.info("testMultipleIdentifierTagFilterEncoder");

    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        List.of(
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue1)),
            new IdentifierTagFilter<>(new IdentifierTag(uuidValue2)))));

    String encodedFilters = encoder.encode();
    String dTags = String.join("\",\"", uuidValue1, uuidValue2);
    assertEquals("{\"#d\":[\"" + dTags + "\"]}", encodedFilters);
  }

//
//  @Test
//  public void testHashTagFiltersEncoder() {
//    log.info("testHashTagFiltersEncoder");
//
//    Integer kind = 1;
//    fail();
//  }
//
//  @Test
//  public void testMultipleHashTagFiltersEncoder() {
//    log.info("testMultipleHashTagFiltersEncoder");
//
//    Integer kind = 1;
//    fail();
//  }

  @Test
  public void testReferencedEventFilterEncoder() {
    log.info("testReferencedEventFilterEncoder");

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new ReferencedEventFilter<>(new EventTag(eventId))));
    String encodedFilters = encoder.encode();
    assertEquals("{\"#e\":[\"" + eventId + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleReferencedEventFilterEncoder() {
    log.info("testMultipleReferencedEventFilterEncoder");

    String eventId1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String eventId2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        List.of(
            new ReferencedEventFilter<>(new EventTag(eventId1)),
            new ReferencedEventFilter<>(new EventTag(eventId2)))));

    String encodedFilters = encoder.encode();
    String eventIds = String.join("\",\"", eventId1, eventId2);
    assertEquals("{\"#e\":[\"" + eventIds + "\"]}", encodedFilters);
  }

  @Test
  public void testReferencedPublicKeyFilterEncoder() {
    log.info("testReferencedPublicKeyFilterEncoder");

    String pubKeyString = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubKeyString)))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"#p\":[\"" + pubKeyString + "\"]}", encodedFilters);
  }

  @Test
  public void testMultipleReferencedPublicKeyFilterEncoder() {
    log.info("testMultipleReferencedPublicKeyFilterEncoder");

    String pubKeyString1 = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String pubKeyString2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubKeyString1))),
        new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(pubKeyString2)))));

    String encodedFilters = encoder.encode();
    String pubKeyTags = String.join("\",\"", pubKeyString1, pubKeyString2);
    assertEquals("{\"#p\":[\"" + pubKeyTags + "\"]}", encodedFilters);
  }

  @Test
  public void testSingleGeohashTagQueryFiltersEncoder() {
    log.info("testSingleGeohashTagQueryFiltersEncoder");

    String new_geohash = "2vghde";

    FiltersEncoder encoder = new FiltersEncoder(
        new Filters(new GeohashTagFilter<>(new GeohashTag(new_geohash))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"#g\":[\"2vghde\"]}", encodedFilters);
  }

  @Test
  public void testMultipleGeohashTagQueryFiltersEncoder() {
    log.info("testMultipleGenericTagQueryFiltersEncoder");

    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
        new GeohashTagFilter<>(new GeohashTag(geohashValue2))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"#g\":[\"2vghde\",\"3abcde\"]}", encodedFilters);
  }

  @Test
  public void testSingleCustomGenericTagQueryFiltersEncoder() {
    log.info("testSingleCustomGenericTagQueryFiltersEncoder");

    String customKey = "#b";
    String customValue = "2vghde";

    FiltersEncoder encoder = new FiltersEncoder(
        new Filters(new GenericTagQueryFilter<>(new GenericTagQuery(customKey, customValue))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"#b\":[\"2vghde\"]}", encodedFilters);
  }

  @Test
  public void testMultipleCustomGenericTagQueryFiltersEncoder() {
    log.info("testMultipleCustomGenericTagQueryFiltersEncoder");

    String customKey = "#b";
    String customValue1 = "2vghde";
    String customValue2 = "3abcde";

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        new GenericTagQueryFilter<>(new GenericTagQuery(customKey, customValue1)),
        new GenericTagQueryFilter<>(new GenericTagQuery(customKey, customValue2))));

    String encodedFilters = encoder.encode();
    assertEquals("{\"#b\":[\"2vghde\",\"3abcde\"]}", encodedFilters);
  }

  @Test
  public void testMultipleAddressableTagFilterEncoder() {
    log.info("testMultipleAddressableTagFilterEncoder");

    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
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

    FiltersEncoder encoder = new FiltersEncoder(new Filters(
        new AddressableTagFilter<>(addressTag1),
        new AddressableTagFilter<>(addressTag2)));

    String encoded = encoder.encode();
    String addressableTags = String.join("\",\"", addressableTag1, addressableTag2);
    assertEquals("{\"#a\":[\"" + addressableTags + "\"]}", encoded);
  }

  @Test
  public void testSinceFiltersEncoder() {
    log.info("testSinceFiltersEncoder");

    Long since = Date.from(Instant.now()).getTime();

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new SinceFilter(since)));
    String encodedFilters = encoder.encode();
    assertEquals("{\"since\":" + since + "}", encodedFilters);
  }

  @Test
  public void testUntilFiltersEncoder() {
    log.info("testUntilFiltersEncoder");

    Long until = Date.from(Instant.now()).getTime();

    FiltersEncoder encoder = new FiltersEncoder(new Filters(new UntilFilter(until)));
    String encodedFilters = encoder.encode();
    assertEquals("{\"until\":" + until + "}", encodedFilters);
  }

  @Test
  public void testReqMessageEmptyFilters() {
    log.info("testReqMessageEmptyFilters");
    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";

    assertThrows(IllegalArgumentException.class, () -> new ReqMessage(subscriptionId, new Filters(List.of())));
  }

  @Test
  public void testReqMessageCustomGenericTagFilter() {
    log.info("testReqMessageEmptyFilterKey");
    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";

    assertDoesNotThrow(() ->
        new ReqMessage(subscriptionId, new Filters(
            new GenericTagQueryFilter<>(new GenericTagQuery("some-tag", "some-value")))));
  }
}
