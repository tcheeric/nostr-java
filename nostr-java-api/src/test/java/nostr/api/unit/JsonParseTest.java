package nostr.api.unit;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP01;
import nostr.api.util.JsonComparator;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.Kind;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.filter.AddressTagFilter;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.GeohashTagFilter;
import nostr.event.filter.HashtagTagFilter;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.filter.KindFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.filter.ReferencedPublicKeyFilter;
import nostr.event.filter.VoteTagFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.VoteTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

/**
 * @author eric
 */
@Slf4j
public class JsonParseTest {
  @Test
  public void testBaseMessageDecoderEventFilter() throws JsonProcessingException {

    String eventId = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    final String parseTarget =
        "[\"REQ\", "
            + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
            + "{\"kinds\": [1], "
            + "\"ids\": [\""
            + eventId
            + "\"],"
            + "\"#p\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

    final var message = new BaseMessageDecoder<>().decode(parseTarget);

    assertEquals(Command.REQ.toString(), message.getCommand());
    assertEquals(
        "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh",
        ((ReqMessage) message).getSubscriptionId());
    assertEquals(1, ((ReqMessage) message).getFiltersList().size());

    Filters filters = ((ReqMessage) message).getFiltersList().getFirst();

    List<Filterable> kindFilters = filters.getFilterByType(KindFilter.FILTER_KEY);
    assertEquals(1, kindFilters.size());
    assertEquals(new KindFilter<>(Kind.TEXT_NOTE), kindFilters.getFirst());

    List<Filterable> eventFilter = filters.getFilterByType(EventFilter.FILTER_KEY);
    assertEquals(1, eventFilter.size());
    assertEquals(
        new EventFilter<>(
            new GenericEvent("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75")),
        eventFilter.getFirst());

    List<Filterable> referencedPublicKeyfilter =
        filters.getFilterByType(ReferencedPublicKeyFilter.FILTER_KEY);
    assertEquals(1, referencedPublicKeyfilter.size());
    assertEquals(
        new ReferencedPublicKeyFilter<>(
            new PubKeyTag(
                new PublicKey("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712"))),
        referencedPublicKeyfilter.getFirst());
  }

  @Test
  public void testBaseMessageDecoderKindsAuthorsReferencedPublicKey()
      throws JsonProcessingException {

    final String parseTarget =
        "[\"REQ\", "
            + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
            + "{\"kinds\": [1], "
            + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
            + "\"#p\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

    final var message = new BaseMessageDecoder<>().decode(parseTarget);

    assertEquals(Command.REQ.toString(), message.getCommand());
    assertEquals(
        "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh",
        ((ReqMessage) message).getSubscriptionId());
    assertEquals(1, ((ReqMessage) message).getFiltersList().size());

    Filters filters = ((ReqMessage) message).getFiltersList().getFirst();

    List<Filterable> kindFilters = filters.getFilterByType(KindFilter.FILTER_KEY);
    assertEquals(1, kindFilters.size());
    assertEquals(new KindFilter<>(Kind.TEXT_NOTE), kindFilters.getFirst());

    List<Filterable> authorFilters = filters.getFilterByType(AuthorFilter.FILTER_KEY);
    assertEquals(1, authorFilters.size());
    assertEquals(
        new AuthorFilter<>(
            new PublicKey("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75")),
        authorFilters.getFirst());

    List<Filterable> referencedPublicKeyfilter =
        filters.getFilterByType(ReferencedPublicKeyFilter.FILTER_KEY);
    assertEquals(1, referencedPublicKeyfilter.size());
    assertEquals(
        new ReferencedPublicKeyFilter<>(
            new PubKeyTag(
                new PublicKey("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712"))),
        referencedPublicKeyfilter.getFirst());
  }

  @Test
  public void testBaseMessageDecoderKindsAuthorsReferencedEvents() throws JsonProcessingException {

    final String parseTarget =
        "[\"REQ\", "
            + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", "
            + "{\"kinds\": [1], "
            + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
            + "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

    final var message = new BaseMessageDecoder<>().decode(parseTarget);

    assertEquals(Command.REQ.toString(), message.getCommand());
    assertEquals(
        "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh",
        ((ReqMessage) message).getSubscriptionId());
    assertEquals(1, ((ReqMessage) message).getFiltersList().size());

    Filters filters = ((ReqMessage) message).getFiltersList().getFirst();

    List<Filterable> kindFilters = filters.getFilterByType(KindFilter.FILTER_KEY);
    assertEquals(1, kindFilters.size());
    assertEquals(new KindFilter<>(Kind.TEXT_NOTE), kindFilters.getFirst());

    List<Filterable> authorFilters = filters.getFilterByType(AuthorFilter.FILTER_KEY);
    assertEquals(1, authorFilters.size());
    assertEquals(
        new AuthorFilter<>(
            new PublicKey("f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75")),
        authorFilters.getFirst());

    List<Filterable> referencedEventFilters =
        filters.getFilterByType(ReferencedEventFilter.FILTER_KEY);
    assertEquals(1, referencedEventFilters.size());
    assertEquals(
        new ReferencedEventFilter<>(
            new EventTag("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712")),
        referencedEventFilters.getFirst());
  }

  @Test
  public void testBaseReqMessageDecoder() throws JsonProcessingException {

    var publicKey = Identity.generateRandomIdentity().getPublicKey();

    final var expectedReqMessage =
        new ReqMessage(
            publicKey.toString(),
            new Filters(
                new KindFilter<>(Kind.SET_METADATA),
                new KindFilter<>(Kind.TEXT_NOTE),
                new KindFilter<>(Kind.CONTACT_LIST),
                new KindFilter<>(Kind.DELETION),
                new AuthorFilter<>(publicKey)));

    String jsonMessage = expectedReqMessage.encode();

    String jsonMsg = jsonMessage.substring(1, jsonMessage.length() - 1);

    System.out.println(jsonMessage);

    String[] parts = jsonMsg.split(",");
    assertEquals("\"REQ\"", parts[0]);
    assertEquals("\"" + publicKey.toHexString() + "\"", parts[1]);
    assertFalse(parts[2].startsWith("["));
    assertFalse(parts[parts.length - 1].endsWith("]"));

    ReqMessage actualMessage = new BaseMessageDecoder<ReqMessage>().decode(jsonMessage);

    assertEquals(jsonMessage, actualMessage.encode());
    assertEquals(expectedReqMessage, actualMessage);
  }

  @Test
  public void testBaseEventMessageDecoder() throws JsonProcessingException {

    final String parseTarget =
        "[\"EVENT\",\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\",{"
            + "\"content\":\"直んないわ。まあええか\",\"created_at\":1686199583,"
            + "\"id\":\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\","
            + "\"kind\":1,"
            + "\"pubkey\":\"8c59239319637f97e007dad0d681e65ce35b1ace333b629e2d33f9465c132608\","
            + "\"sig\":\"9584afd231c52fcbcec6ce668a2cc4b6dc9b4d9da20510dcb9005c6844679b4844edb7a2e1e0591958b0295241567c774dbf7d39a73932877542de1a5f963f4b\","
            + "\"tags\":[]}]";

    final var message = new BaseMessageDecoder<>().decode(parseTarget);

    assertEquals(Command.EVENT.toString(), message.getCommand());

    final var event = (GenericEvent) (((EventMessage) message).getEvent());
    assertEquals(
        "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh",
        ((EventMessage) message).getSubscriptionId());
    assertEquals(1, event.getKind().intValue());
    assertEquals(1686199583, event.getCreatedAt().longValue());
    assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());
  }

  @Test
  public void testBaseEventMessageMarkerDecoder() throws JsonProcessingException {

    final String json =
        "[\"EVENT\",\"temp20230627\",{"
            + "\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\","
            + "\"kind\":1,"
            + "\"pubkey\":\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\","
            + "\"created_at\":1687765220,\"content\":\"手順書が間違ってたら作業者は無理だな\",\"tags\":["
            + "[\"e\",\"494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346\",\"\",\"root\"],"
            + "[\"p\",\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\"]],"
            + "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\""
            + "}]";

    BaseMessage message = new BaseMessageDecoder<>().decode(json);

    final var event = (GenericEvent) (((EventMessage) message).getEvent());
    var tags = event.getTags();
    for (BaseTag t : tags) {
      if (t.getCode().equalsIgnoreCase("e")) {
        EventTag et = (EventTag) t;
        assertEquals(Marker.ROOT, et.getMarker());
      }
    }
  }

  @Test
  public void testGenericTagDecoder() {
    final String jsonString = "[\"saturn\", \"jetpack\", false]";

    var tag = new GenericTagDecoder<>().decode(jsonString);

    assertEquals("saturn", tag.getCode());
    assertEquals(2, tag.getAttributes().size());
    assertEquals("jetpack", ((ElementAttribute) (tag.getAttributes().toArray())[0]).value());
    assertEquals(
        false,
        Boolean.valueOf(
            ((ElementAttribute) (tag.getAttributes().toArray())[1]).value().toString()));
  }

  @Test
  public void testClassifiedListingTagSerializer() throws JsonProcessingException {
    final String classifiedListingEventJson =
        "{\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\",\"kind\":30402,\"content\":\"content"
            + " ipsum\","
            + "\"pubkey\":\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\","
            + "\"created_at\":1687765220,\"tags\":["
            + "[\"p\",\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\"],[\"title\",\"title"
            + " ipsum\"],[\"summary\",\"summary"
            + " ipsum\"],[\"published_at\",\"1687765220\"],[\"location\",\"location ipsum\"],"
            + "[\"price\",\"11111\",\"BTC\",\"1\"]],"
            + "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\""
            + "}]";

    GenericEvent event = new GenericEventDecoder<>().decode(classifiedListingEventJson);
    EventMessage message = NIP01.createEventMessage(event, "1");
    assertEquals(1, message.getNip());
    String encoded = new BaseEventEncoder<>((BaseEvent) message.getEvent()).encode();
    assertEquals(
        "{\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\",\"kind\":30402,\"content\":\"content"
            + " ipsum\",\"pubkey\":\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\",\"created_at\":1687765220,\"tags\":[[\"p\",\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\"],[\"title\",\"title"
            + " ipsum\"],[\"summary\",\"summary"
            + " ipsum\"],[\"published_at\",\"1687765220\"],[\"location\",\"location"
            + " ipsum\"],[\"price\",\"11111\",\"BTC\",\"1\"]],\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}",
        encoded);

    assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", event.getId());
    assertEquals(30402, event.getKind());
    assertEquals("content ipsum", event.getContent());
    assertEquals(
        "ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de",
        event.getPubKey().toString());
    assertEquals(1687765220L, event.getCreatedAt());
    assertEquals(
        "86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546",
        event.getSignature().toString());

    assertEquals(
        new BigDecimal("11111"),
        event.getTags().stream()
            .filter(baseTag -> baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getNumber)
            .findFirst()
            .orElseThrow());

    assertEquals(
        "BTC",
        event.getTags().stream()
            .filter(baseTag -> baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getCurrency)
            .findFirst()
            .orElseThrow());

    assertEquals(
        "1",
        event.getTags().stream()
            .filter(baseTag -> baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getFrequency)
            .findFirst()
            .orElseThrow());

    List<GenericTag> genericTags =
        event.getTags().stream()
            .filter(GenericTag.class::isInstance)
            .map(GenericTag.class::cast)
            .toList();

    assertEquals(
        "title ipsum",
        genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("title"))
            .map(GenericTag::getAttributes)
            .toList()
            .getFirst()
            .getFirst()
            .value());

    assertEquals(
        "summary ipsum",
        genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("summary"))
            .map(GenericTag::getAttributes)
            .toList()
            .getFirst()
            .getFirst()
            .value());

    assertEquals(
        "1687765220",
        genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("published_at"))
            .map(GenericTag::getAttributes)
            .toList()
            .getFirst()
            .getFirst()
            .value());

    assertEquals(
        "location ipsum",
        genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("location"))
            .map(GenericTag::getAttributes)
            .toList()
            .getFirst()
            .getFirst()
            .value());
  }

  @Test
  public void testDeserializeTag() throws Exception {

    String npubHex =
        new PublicKey(
                Bech32.fromBech32(
                    "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9"))
            .toString();
    final String jsonString = "[\"p\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
    var tag = new BaseTagDecoder<>().decode(jsonString);

    assertInstanceOf(PubKeyTag.class, tag);

    PubKeyTag pTag = (PubKeyTag) tag;
    assertEquals("wss://nostr.java", pTag.getMainRelayUrl());
    assertEquals(npubHex, pTag.getPublicKey().toString());
    assertEquals("alice", pTag.getPetName());
  }

  @Test
  public void testDeserializeGenericTag() throws Exception {
    String npubHex =
        new PublicKey(
                Bech32.fromBech32(
                    "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9"))
            .toString();
    final String jsonString = "[\"gt\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
    var tag = new BaseTagDecoder<>().decode(jsonString);

    assertInstanceOf(GenericTag.class, tag);

    GenericTag gTag = (GenericTag) tag;
    assertEquals("gt", gTag.getCode());
  }

  @Test
  public void testReqMessageFilterListSerializer() {

    String new_geohash = "2vghde";
    String second_geohash = "3abcde";

    ReqMessage reqMessage =
        new ReqMessage(
            "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9",
            new Filters(
                new GenericTagQueryFilter<>(new GenericTagQuery("#g", new_geohash)),
                new GenericTagQueryFilter<>(new GenericTagQuery("#g", second_geohash))));

    assertDoesNotThrow(
        () -> {
          String jsonMessage = reqMessage.encode();
          String expected =
              "[\"REQ\",\"npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9\",{\"#g\":[\"2vghde\",\"3abcde\"]}]";
          assertEquals(expected, jsonMessage);
        });
  }

  @Test
  public void testReqMessageGeohashTagDeserializer() throws JsonProcessingException {

    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String geohashKey = "#g";
    String geohashValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\",\"" + subscriptionId + "\",{\"" + geohashKey + "\":[\"" + geohashValue + "\"]}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId, new Filters(new GeohashTagFilter<>(new GeohashTag(geohashValue))));

    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessageGeohashFilterListDecoder() {

    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String reqJsonWithCustomTagQueryFiltersToDecode =
        "[\"REQ\",\""
            + subscriptionId
            + "\",{\""
            + geohashKey
            + "\":[\""
            + geohashValue1
            + "\",\""
            + geohashValue2
            + "\"]}]";

    assertDoesNotThrow(
        () -> {
          ReqMessage decodedReqMessage =
              new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFiltersToDecode);

          ReqMessage expectedReqMessage =
              new ReqMessage(
                  subscriptionId,
                  new Filters(
                      new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
                      new GeohashTagFilter<>(new GeohashTag(geohashValue2))));

          assertEquals(reqJsonWithCustomTagQueryFiltersToDecode, decodedReqMessage.encode());
          assertEquals(expectedReqMessage, decodedReqMessage);
        });
  }

  @Test
  public void testReqMessageHashtagTagDeserializer() throws JsonProcessingException {

    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String hashtagKey = "#t";
    String hashtagValue = "2vghde";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\",\"" + subscriptionId + "\",{\"" + hashtagKey + "\":[\"" + hashtagValue + "\"]}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId, new Filters(new HashtagTagFilter<>(new HashtagTag(hashtagValue))));

    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessageHashtagTagFilterListDecoder() {

    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String hashtagKey = "#t";
    String hashtagValue1 = "2vghde";
    String hashtagValue2 = "3abcde";
    String reqJsonWithCustomTagQueryFiltersToDecode =
        "[\"REQ\",\""
            + subscriptionId
            + "\",{\""
            + hashtagKey
            + "\":[\""
            + hashtagValue1
            + "\",\""
            + hashtagValue2
            + "\"]}]";

    assertDoesNotThrow(
        () -> {
          ReqMessage decodedReqMessage =
              new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFiltersToDecode);

          ReqMessage expectedReqMessage =
              new ReqMessage(
                  subscriptionId,
                  new Filters(
                      new HashtagTagFilter<>(new HashtagTag(hashtagValue1)),
                      new HashtagTagFilter<>(new HashtagTag(hashtagValue2))));

          assertEquals(reqJsonWithCustomTagQueryFiltersToDecode, decodedReqMessage.encode());
          assertEquals(expectedReqMessage, decodedReqMessage);
        });
  }

  @Test
  public void testReqMessagePopulatedFilterDecoder() {

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    String kind = "1";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", "
            + "{\"kinds\": ["
            + kind
            + "], "
            + "\"authors\": [\""
            + author
            + "\"],"
            + "\""
            + geohashKey
            + "\": [\""
            + geohashValue1
            + "\",\""
            + geohashValue2
            + "\"],"
            + "\"#e\": [\""
            + referencedEventId
            + "\"],"
            + "\"#p\": [\""
            + author
            + "\"]"
            + "}]";

    assertDoesNotThrow(
        () -> {
          ReqMessage decodedReqMessage =
              new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

          ReqMessage expectedReqMessage =
              new ReqMessage(
                  subscriptionId,
                  new Filters(
                      new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
                      new GeohashTagFilter<>(new GeohashTag(geohashValue2)),
                      new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(author))),
                      new KindFilter<>(Kind.TEXT_NOTE),
                      new AuthorFilter<>(new PublicKey(author)),
                      new ReferencedEventFilter<>(new EventTag(referencedEventId))));

          assertEquals(expectedReqMessage, decodedReqMessage);
        });
  }

  @Test
  public void testReqMessagePopulatedListOfFiltersWithIdentityDecoder()
      throws JsonProcessingException {

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    String kind = "1";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String uuidKey = "#d";
    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", "
            + "{\"kinds\": ["
            + kind
            + "], "
            + "\"authors\": [\""
            + author
            + "\"],"
            + "\""
            + geohashKey
            + "\": [\""
            + geohashValue1
            + "\",\""
            + geohashValue2
            + "\"],"
            + "\""
            + uuidKey
            + "\": [\""
            + uuidValue1
            + "\",\""
            + uuidValue2
            + "\"],"
            + "\"#e\": [\""
            + referencedEventId
            + "\"]}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId,
            new Filters(
                new KindFilter<>(Kind.TEXT_NOTE),
                new AuthorFilter<>(new PublicKey(author)),
                new ReferencedEventFilter<>(new EventTag(referencedEventId)),
                new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
                new GeohashTagFilter<>(new GeohashTag(geohashValue2)),
                new IdentifierTagFilter<>(new IdentifierTag(uuidValue1)),
                new IdentifierTagFilter<>(new IdentifierTag(uuidValue2))));

    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessagePopulatedListOfFiltersListDecoder() throws JsonProcessingException {

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    Integer kind = 1;
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String uuidValue1 = "UUID-1";

    String addressableTag = String.join(":", String.valueOf(kind), author, uuidValue1);

    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", "
            + "{\"kinds\": ["
            + kind
            + "], "
            + "\"authors\": [\""
            + author
            + "\"],"
            + "\"#e\": [\""
            + referencedEventId
            + "\"],"
            + "\"#a\": [\""
            + addressableTag
            + "\"],"
            + "\"#p\": [\""
            + author
            + "\"]"
            + "}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    AddressTag addressTag1 = new AddressTag();
    addressTag1.setKind(kind);
    addressTag1.setPublicKey(new PublicKey(author));
    addressTag1.setIdentifierTag(new IdentifierTag(uuidValue1));

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId,
            new Filters(
                new KindFilter<>(Kind.TEXT_NOTE),
                new AuthorFilter<>(new PublicKey(author)),
                new ReferencedEventFilter<>(new EventTag(referencedEventId)),
                new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(author))),
                new AddressTagFilter<>(addressTag1)));

    assertEquals(expectedReqMessage.encode(), decodedReqMessage.encode());
    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessagePopulatedListOfMultipleTypeFiltersListDecoder()
      throws JsonProcessingException {

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    String kind = "1";
    String kind2 = "2";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String author2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", "
            + "{\"kinds\": ["
            + kind
            + ", "
            + kind2
            + "], "
            + "\"authors\": [\""
            + author
            + "\",\""
            + author2
            + "\"],"
            + "\"#e\": [\""
            + referencedEventId
            + "\"]"
            + "}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId,
            new Filters(
                new KindFilter<>(Kind.TEXT_NOTE),
                new KindFilter<>(Kind.RECOMMEND_SERVER),
                new AuthorFilter<>(new PublicKey(author)),
                new AuthorFilter<>(new PublicKey(author2)),
                new ReferencedEventFilter<>(new EventTag(referencedEventId))));

    assertEquals(expectedReqMessage.encode(), decodedReqMessage.encode());
    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testGenericTagQueryListDecoder() throws JsonProcessingException {

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    String kind = "1";
    String kind2 = "2";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String author2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String uuidKey = "#d";
    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", "
            + "{\"kinds\": ["
            + kind
            + ", "
            + kind2
            + "], "
            + "\"authors\": [\""
            + author
            + "\",\""
            + author2
            + "\"],"
            + "\""
            + geohashKey
            + "\": [\""
            + geohashValue1
            + "\",\""
            + geohashValue2
            + "\"],"
            + "\""
            + uuidKey
            + "\": [\""
            + uuidValue1
            + "\",\""
            + uuidValue2
            + "\"],"
            + "\"#e\": [\""
            + referencedEventId
            + "\"]"
            + "}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    ReqMessage expectedReqMessage =
        new ReqMessage(
            subscriptionId,
            new Filters(
                new KindFilter<>(Kind.TEXT_NOTE),
                new KindFilter<>(Kind.RECOMMEND_SERVER),
                new AuthorFilter<>(new PublicKey(author)),
                new AuthorFilter<>(new PublicKey(author2)),
                new ReferencedEventFilter<>(new EventTag(referencedEventId)),
                new GeohashTagFilter<>(new GeohashTag(geohashValue1)),
                new GeohashTagFilter<>(new GeohashTag(geohashValue2)),
                new IdentifierTagFilter<>(new IdentifierTag(uuidValue1)),
                new IdentifierTagFilter<>(new IdentifierTag(uuidValue2))));

    assertTrue(
        JsonComparator.isEquivalentJson(
            MAPPER_BLACKBIRD
                .createArrayNode()
                .add(MAPPER_BLACKBIRD.readTree(expectedReqMessage.encode())),
            MAPPER_BLACKBIRD
                .createArrayNode()
                .add(MAPPER_BLACKBIRD.readTree(decodedReqMessage.encode()))));
    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessageAddressableTagDeserializer() throws JsonProcessingException {

    Integer kind = 1;
    String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String uuidKey = "#a";
    String uuidValue1 = "UUID-1";

    String joined1 = String.join(":", String.valueOf(kind), author, uuidValue1);

    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\",\"" + subscriptionId + "\",{\"" + uuidKey + "\":[\"" + joined1 + "\"]}]";

    ReqMessage decodedReqMessage =
        new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    AddressTag addressTag1 = new AddressTag();
    addressTag1.setKind(kind);
    addressTag1.setPublicKey(new PublicKey(author));
    addressTag1.setIdentifierTag(new IdentifierTag(uuidValue1));

    ReqMessage expectedReqMessage =
        new ReqMessage(subscriptionId, new Filters(new AddressTagFilter<>(addressTag1)));

    assertEquals(expectedReqMessage.encode(), decodedReqMessage.encode());
    assertEquals(expectedReqMessage, decodedReqMessage);
  }

  @Test
  public void testReqMessageSubscriptionIdTooLong() {

    String malformedSubscriptionId =
        "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujhaa";
    final String parseTarget =
        "[\"REQ\", "
            + "\""
            + malformedSubscriptionId
            + "\", "
            + "{\"kinds\": [1], "
            + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
            + "\"#p\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

    assertThrows(
        IllegalArgumentException.class, () -> new BaseMessageDecoder<>().decode(parseTarget));
  }

  @Test
  public void testReqMessageSubscriptionIdTooShort() {

    String malformedSubscriptionId = "";
    final String parseTarget =
        "[\"REQ\", "
            + "\""
            + malformedSubscriptionId
            + "\", "
            + "{\"kinds\": [1], "
            + "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"],"
            + "\"#p\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

    assertThrows(
        IllegalArgumentException.class, () -> new BaseMessageDecoder<>().decode(parseTarget));
  }

  @Test
  public void testBaseEventMessageDecoderMultipleFiltersJson() throws JsonProcessingException {

    final String eventJson =
        "[\"EVENT\",{\"content\":\"直ん直んないわ。まあええか\",\"created_at\":1786199583,"
            + "\"id\":\"ec7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\","
            + "\"kind\":1,"
            + "\"pubkey\":\"9c59239319637f97e007dad0d681e65ce35b1ace333b629e2d33f9465c132608\","
            + "\"sig\":\"9584afd231c52fcbcec6ce668a2cc4b6dc9b4d9da20510dcb9005c6844679b4844edb7a2e1e0591958b0295241567c774dbf7d39a73932877542de1a5f963f4b\","
            + "\"tags\":[]}]";

    final var eventMessage = new BaseMessageDecoder<>().decode(eventJson);

    assertEquals(Command.EVENT.toString(), eventMessage.getCommand());

    final var event = (GenericEvent) (((EventMessage) eventMessage).getEvent());
    assertEquals(1, event.getKind().intValue());
    assertEquals(1786199583, event.getCreatedAt().longValue());
    assertEquals("ec7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());

    String subscriptionId = "npub27x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    final String requestJson =
        "[\"REQ\", "
            + "\""
            + subscriptionId
            + "\", {\"kinds\": [1], \"authors\":"
            + " [\"9c59239319637f97e007dad0d681e65ce35b1ace333b629e2d33f9465c132608\"]},"
            + // first filter set
            "{\"kinds\": [1], \"#p\":"
            + " [\"ec7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}"
            + // second filter set
            "]";

    final var message = new BaseMessageDecoder<>().decode(requestJson);

    assertEquals(Command.REQ.toString(), message.getCommand());
    assertEquals(subscriptionId, ((ReqMessage) message).getSubscriptionId());
    assertEquals(2, ((ReqMessage) message).getFiltersList().size());
  }

  @Test
  public void testReqMessageVoteTagFilterDecoder() {

    String subscriptionId = "npub333k6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
    String voteTagKey = "#v";
    Integer voteTagValue = 1;
    String reqJsonWithVoteTagFilterToDecode =
        "[\"REQ\",\"" + subscriptionId + "\",{\"" + voteTagKey + "\":[\"" + voteTagValue + "\"]}]";

    assertDoesNotThrow(
        () -> {
          ReqMessage decodedReqMessage =
              new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithVoteTagFilterToDecode);

          ReqMessage expectedReqMessage =
              new ReqMessage(
                  subscriptionId, new Filters(new VoteTagFilter<>(new VoteTag(voteTagValue))));

          assertEquals(reqJsonWithVoteTagFilterToDecode, decodedReqMessage.encode());
          assertEquals(expectedReqMessage, decodedReqMessage);
        });
  }
}
