package nostr.test.event.filter;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.Kind;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log
public class FiltersTest {

  @Test
  public void testEventFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
    GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
    instance.update();

    Filters filters = new Filters();
    GenericEvent eventList = new GenericEvent();
    eventList.setId(instance.getId());

    filters.setEvents(List.of(eventList));

    List<GenericEvent> filtersEvents = filters.getEvents();
    assertEquals(filtersEvents.getFirst().getId(), eventList.getId());
  }

  @Test
  public void testAuthorFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
    GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
    instance.update();

    Filters filters = new Filters();
    filters.setAuthors(List.of(publicKey));

    List<PublicKey> authors = filters.getAuthors();
    assertEquals(authors, List.of(publicKey));
  }

  @Test
  public void testKindFilters() {
    Filters filters = new Filters();
    List<Kind> kindList = List.of(Kind.CONTACT_LIST, Kind.DELETION);
    filters.setKinds(kindList);
    List<Kind> kinds = filters.getKinds();
    assertEquals(kinds, kindList);
  }

  @Test
  public void testReferencedEventsFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();

    GenericEvent eventToReference = EntityFactory.Events.createTextNoteEvent(publicKey);
    eventToReference.update();

    GenericEvent eventContainingEventToReference = EntityFactory.Events.createTextNoteEvent(publicKey);
    eventContainingEventToReference.setTags(List.of(new EventTag(eventToReference.getId())));
    eventContainingEventToReference.update();

    Filters filters = new Filters();
    filters.setReferencedEvents(List.of(eventContainingEventToReference));

    filters.getReferencedEvents().getFirst().getTags().stream()
        .filter(EventTag.class::isInstance)
        .map(EventTag.class::cast)
        .findFirst()
        .ifPresent(eventTag ->
            assertEquals(eventTag.getIdEvent(), eventToReference.getId()));
  }

  @Test
  public void testReferencedPubkeysFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();

    GenericEvent eventToReference = EntityFactory.Events.createTextNoteEvent(publicKey);
    eventToReference.update();

    GenericEvent eventContainingEventToReference = EntityFactory.Events.createTextNoteEvent(publicKey);
    eventContainingEventToReference.setTags(List.of(new PubKeyTag(eventToReference.getPubKey())));
    eventContainingEventToReference.update();

    Filters filters = new Filters();
    filters.setReferencedEvents(List.of(eventContainingEventToReference));

    filters.getReferencedEvents().getFirst().getTags().stream()
        .filter(PubKeyTag.class::isInstance)
        .map(PubKeyTag.class::cast)
        .findFirst()
        .ifPresent(pubkeyTag ->
            assertEquals(pubkeyTag.getPublicKey().toHexString(), eventToReference.getPubKey().toHexString()));
  }

  @Test
  public void testSinceFilters() {
    Filters filters = new Filters();
    long sinceTime = Date.from(Instant.now()).getTime();
    filters.setSince(sinceTime);

    assertEquals(sinceTime, filters.getSince());
  }

  @Test
  public void testUntilFilters() {
    Filters filters = new Filters();
    long untilTime = Date.from(Instant.now()).getTime();
    filters.setUntil(untilTime);

    assertEquals(untilTime, filters.getUntil());
  }

  @Test
  public void testGenericQueryTagFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
    GenericTagQuery genericTagQuery = new GenericTagQuery();
    String key = "#a";
    genericTagQuery.setTagName(key);

    String kind = Kind.TEXT_NOTE.toString();
    String hexString = publicKey.toHexString();
    String uuid = new IdentifierTag("uuid").getId();
    String relayUri = new Relay("ws://localhost:5555").getUri();

    List<String> addressTagValues = List.of(
        kind,
        hexString,
        uuid,
        relayUri
    );
    genericTagQuery.setValue(addressTagValues);

    Filters filters = new Filters();
    filters.setGenericTagQuery(
        genericTagQuery.getTagName(),
        addressTagValues);

    Map<String, List<String>> genericTagQuery1 = filters.getGenericTagQuery(key);
    log.info(genericTagQuery1.entrySet().toString());

    assertTrue(genericTagQuery1.get(key).contains(kind));
    assertTrue(genericTagQuery1.get(key).contains(hexString));
    assertTrue(genericTagQuery1.get(key).contains(uuid));
    assertTrue(genericTagQuery1.get(key).contains(relayUri));
  }

  @Test
  public void testMultipleFilters() {
    PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
    GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
    instance.update();

    GenericEvent eventList = new GenericEvent();
    eventList.setId(instance.getId());

    List<Kind> kindList = List.of(Kind.CONTACT_LIST, Kind.DELETION);

    GenericEvent eventContainingEventToReference = EntityFactory.Events.createTextNoteEvent(publicKey);
    eventContainingEventToReference.setTags(List.of(new EventTag(instance.getId())));
    eventContainingEventToReference.setTags(List.of(new PubKeyTag(instance.getPubKey())));
    eventContainingEventToReference.update();

    long sinceTime = Date.from(Instant.now()).getTime();
    long untilTime = Date.from(Instant.now()).getTime();

    Filters filters = new Filters();
    filters.setEvents(List.of(eventList));
    filters.setAuthors(List.of(publicKey));
    filters.setKinds(kindList);
    filters.setReferencedEvents(List.of(eventContainingEventToReference));
    filters.setSince(sinceTime);
    filters.setUntil(untilTime);

    GenericTagQuery genericTagQuery = new GenericTagQuery();
    String key = "#a";
    genericTagQuery.setTagName(key);

    String kind = Kind.TEXT_NOTE.toString();
    String hexString = publicKey.toHexString();
    String uuid = new IdentifierTag("uuid").getId();
    String relayUri = new Relay("ws://localhost:5555").getUri();

    List<String> addressTagValues = List.of(
        kind,
        hexString,
        uuid,
        relayUri
    );
    genericTagQuery.setValue(addressTagValues);

    filters.setGenericTagQuery(
        genericTagQuery.getTagName(),
        addressTagValues);

    List<GenericEvent> filtersEvents = filters.getEvents();
    assertEquals(filtersEvents.getFirst().getId(), eventList.getId());

    List<PublicKey> authors = filters.getAuthors();
    assertEquals(authors, List.of(publicKey));

    List<Kind> kinds = filters.getKinds();
    assertEquals(kinds, kindList);

    filters.getReferencedEvents().getFirst().getTags().stream()
        .filter(EventTag.class::isInstance)
        .map(EventTag.class::cast)
        .findFirst()
        .ifPresent(eventTag ->
            assertEquals(eventTag.getIdEvent(), instance.getId()));

    filters.getReferencedEvents().getFirst().getTags().stream()
        .filter(PubKeyTag.class::isInstance)
        .map(PubKeyTag.class::cast)
        .findFirst()
        .ifPresent(pubkeyTag ->
            assertEquals(pubkeyTag.getPublicKey().toHexString(), instance.getPubKey().toHexString()));

    assertEquals(sinceTime, filters.getSince());
    assertEquals(untilTime, filters.getUntil());

    Map<String, List<String>> genericTagQuery1 = filters.getGenericTagQuery(key);

    assertTrue(genericTagQuery1.get(key).contains(kind));
    assertTrue(genericTagQuery1.get(key).contains(hexString));
    assertTrue(genericTagQuery1.get(key).contains(uuid));
    assertTrue(genericTagQuery1.get(key).contains(relayUri));
  }
}
