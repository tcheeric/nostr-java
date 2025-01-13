package nostr.test.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import nostr.api.NIP01;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Marker;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author eric
 */
@Log
public class JsonParseTest {

    @Test
    public void testBaseMessageDecoder() throws JsonProcessingException {
        log.info("testBaseMessageDecoder");

        final String parseTarget =
            "[\"REQ\", " +
                "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", " +
                "{\"kinds\": [1], " +
                "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"]," +
                "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

        final var message = new BaseMessageDecoder<>().decode(parseTarget);

        assertEquals(Command.REQ.toString(), message.getCommand());
        assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", ((ReqMessage) message).getSubscriptionId());
        assertEquals(1, ((ReqMessage) message).getFiltersList().size());

        var filters = ((ReqMessage) message).getFiltersList().get(0);

        assertEquals(1, filters.getKinds().size());
        assertEquals(Kind.TEXT_NOTE, filters.getKinds().get(0));

        assertEquals(1, filters.getAuthors().size());
        assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", filters.getAuthors().get(0).toBech32String());

        assertEquals(1, filters.getReferencedEvents().size());
        assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", (filters.getReferencedEvents().get(0).getId()));
    }

    @Test
    public void testBaseReqMessageDecoder() {
        log.info("testBaseReqMessageDecoder");

        final var filtersList = new ArrayList<Filters>();
        var publicKey = Identity.generateRandomIdentity().getPublicKey();
        filtersList.add(Filters.builder().authors(new ArrayList<>(List.of(publicKey))).kinds(new ArrayList<>(List.of(Kind.CONTACT_LIST, Kind.DELETION))).build());
        filtersList.add(Filters.builder().kinds(new ArrayList<>(List.of(Kind.SET_METADATA, Kind.TEXT_NOTE))).build());
        final var reqMessage = new ReqMessage(publicKey.toString(), filtersList);

        assertDoesNotThrow(() -> {
            String jsonMessage = reqMessage.encode();

            String jsonMsg = jsonMessage.substring(1, jsonMessage.length() - 1);
            String[] parts = jsonMsg.split(",");
            assertEquals("\"REQ\"", parts[0]);
            assertEquals("\"" + publicKey.toString() + "\"", parts[1]);
            assertFalse(parts[2].startsWith("["));
            assertFalse(parts[parts.length - 1].endsWith("]"));

            BaseMessage message = new BaseMessageDecoder<>().decode(jsonMessage);

            assertEquals(reqMessage, message);
        });
    }

    @Test
    public void testBaseEventMessageDecoder() throws JsonProcessingException {
        log.info("testBaseEventMessageDecoder");

        final String parseTarget
            = "[\"EVENT\","
            + "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\","
            + "{"
            + "\"content\":\"直んないわ。まあええか\","
            + "\"created_at\":1686199583,"
            + "\"id\":\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\","
            + "\"kind\":1,"
            + "\"pubkey\":\"8c59239319637f97e007dad0d681e65ce35b1ace333b629e2d33f9465c132608\","
            + "\"sig\":\"9584afd231c52fcbcec6ce668a2cc4b6dc9b4d9da20510dcb9005c6844679b4844edb7a2e1e0591958b0295241567c774dbf7d39a73932877542de1a5f963f4b\","
            + "\"tags\":[]"
            + "}]";

        final var message = new BaseMessageDecoder<>().decode(parseTarget);

        assertEquals(Command.EVENT.toString(), message.getCommand());

        final var event = (GenericEvent) (((EventMessage) message).getEvent());
        assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", ((EventMessage) message).getSubscriptionId());
        assertEquals(1, event.getKind().intValue());
        assertEquals(1686199583, event.getCreatedAt().longValue());
        assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());
    }

    @Test
    public void testBaseEventMessageMarkerDecoder() throws JsonProcessingException {
        log.info("testBaseEventMessageMarkerDecoder");

        final String json = "["
            + "\"EVENT\","
            + "\"temp20230627\","
            + "{"
            + "\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\","
            + "\"kind\":1,"
            + "\"pubkey\":\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\","
            + "\"created_at\":1687765220,"
            + "\"content\":\"手順書が間違ってたら作業者は無理だな\","
            + "\"tags\":["
            + "[\"e\",\"494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346\",\"\",\"root\"],"
            + "[\"p\",\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\"]"
            + "],"
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
        log.info("testGenericTagDecoder");
        final String jsonString = "[\"saturn\", \"jetpack\", false]";

        var tag = new GenericTagDecoder<>().decode(jsonString);

        assertEquals("saturn", tag.getCode());
        assertEquals(2, tag.getAttributes().size());
        assertEquals("jetpack", ((ElementAttribute) (tag.getAttributes().toArray())[0]).getValue());
        assertEquals(false, Boolean.valueOf(((ElementAttribute) (tag.getAttributes().toArray())[1]).getValue().toString()));
    }

    @Test
    public void testClassifiedListingTagSerializer() {
        log.info("testClassifiedListingSerializer");
        final String classifiedListingEventJson = "{"
            + "\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\","
            + "\"kind\":30402,"
            + "\"content\":\"content ipsum\","
            + "\"pubkey\":\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\","
            + "\"created_at\":1687765220,"
            + "\"tags\":["
            + "[\"p\",\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\"],"
            + "[\"title\",\"title ipsum\"],"
            + "[\"summary\",\"summary ipsum\"],"
            + "[\"published_at\",\"1687765220\"],"
            + "[\"location\",\"location ipsum\"],"
            + "[\"price\",\"11111\",\"BTC\",\"1\"]],"
            + "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\""
            + "}]";

        GenericEvent event = new GenericEventDecoder<>().decode(classifiedListingEventJson);
        EventMessage message = NIP01.createEventMessage(event, "1");
        assertEquals(1, message.getNip());
        String encoded = new BaseEventEncoder<>((BaseEvent) message.getEvent()).encode();
        assertEquals("{\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\",\"kind\":30402,\"content\":\"content ipsum\",\"pubkey\":\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\",\"created_at\":1687765220,\"tags\":[[\"p\",\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\"],[\"title\",\"title ipsum\"],[\"summary\",\"summary ipsum\"],[\"published_at\",\"1687765220\"],[\"location\",\"location ipsum\"],[\"price\",\"11111\",\"BTC\",\"1\"]],\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}", encoded);

        assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", event.getId());
        assertEquals(30402, event.getKind());
        assertEquals("content ipsum", event.getContent());
        assertEquals("ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de", event.getPubKey().toString());
        assertEquals(1687765220L, event.getCreatedAt());
        assertEquals("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546", event.getSignature().toString());

        assertEquals(new BigDecimal("11111"), event.getTags().stream().filter(baseTag ->
                baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getNumber).findFirst().orElseThrow());

        assertEquals("BTC", event.getTags().stream().filter(baseTag ->
                baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getCurrency).findFirst().orElseThrow());

        assertEquals("1", event.getTags().stream().filter(baseTag ->
                baseTag.getCode().equalsIgnoreCase("price"))
            .filter(PriceTag.class::isInstance)
            .map(PriceTag.class::cast)
            .map(PriceTag::getFrequency).findFirst().orElseThrow());

        List<GenericTag> genericTags = event.getTags().stream()
            .filter(GenericTag.class::isInstance)
            .map(GenericTag.class::cast).toList();

        assertEquals("title ipsum", genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("title")).map(GenericTag::getAttributes).toList().get(0).get(0).getValue());

        assertEquals("summary ipsum", genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("summary")).map(GenericTag::getAttributes).toList().get(0).get(0).getValue());

        assertEquals("1687765220", genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("published_at")).map(GenericTag::getAttributes).toList().get(0).get(0).getValue());

        assertEquals("location ipsum", genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("location")).map(GenericTag::getAttributes).toList().get(0).get(0).getValue());
    }

    @Test
    public void testDeserializeTag() {
        log.info("testDeserializeTag");

        assertDoesNotThrow(() -> {
            String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
            final String jsonString = "[\"p\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
            var tag = new BaseTagDecoder<>().decode(jsonString);

            assertTrue(tag instanceof PubKeyTag);

            PubKeyTag pTag = (PubKeyTag) tag;
            assertEquals("wss://nostr.java", pTag.getMainRelayUrl());
            assertEquals(npubHex, pTag.getPublicKey().toString());
            assertEquals("alice", pTag.getPetName());
        });
    }

    @Test
    public void testDeserializeGenericTag() {
        log.info("testDeserializeGenericTag");
        assertDoesNotThrow(() -> {
            String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
            final String jsonString = "[\"gt\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
            var tag = new BaseTagDecoder<>().decode(jsonString);

            assertTrue(tag instanceof GenericTag);

            GenericTag gTag = (GenericTag) tag;
            assertEquals("gt", gTag.getCode());
        });
    }

    @Test
    public void testFiltersEncoder() {
        log.info("testFiltersEncoder");

        String new_geohash = "2vghde";
        List<String> geohashList = new ArrayList<>();
        geohashList.add(new_geohash);
        Filters filters = Filters.builder().genericTagQuery(Map.of("#g", geohashList)).build();

        FiltersEncoder encoder = new FiltersEncoder(filters);
        String jsonMessage = encoder.encode();
        assertEquals("{\"#g\":[\"2vghde\"]}", jsonMessage);
    }

    @Test
    public void testReqMessageFilterListSerializer() {
        log.info("testReqMessageFilterListSerializer");

        String new_geohash = "2vghde";
        String second_geohash = "3abcde";
        List<String> geohashList = new ArrayList<>();
        geohashList.add(new_geohash);
        geohashList.add(second_geohash);
        Filters filters = Filters.builder().genericTagQuery(Map.of("#g", geohashList)).build();

        ReqMessage reqMessage = new ReqMessage("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9", new ArrayList<Filters>(List.of(filters)));
        assertDoesNotThrow(() -> {
            String jsonMessage = reqMessage.encode();

            assertEquals("[\"REQ\",\"npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9\",{\"#g\":[\"2vghde\",\"3abcde\"]}]", jsonMessage);
        });
    }

    @Test
    public void testReqMessageFiltersDecoder() {
        log.info("testReqMessageFiltersDecoder");

        String geohashKey = "#g";
        String geohashValue = "2vghde";
        String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue + "\"]}";

        Filters decodedFilters = new FiltersDecoder<>().decode(reqJsonWithCustomTagQueryFilterToDecode);

        Filters expectedFilters = new Filters();
        List<String> expectedGeohashValueList = List.of(geohashValue);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValueList);

        assertEquals(expectedFilters, decodedFilters);
    }

    @Test
    public void testReqMessageFiltersListDecoder() {
        log.info("testReqMessageFiltersListDecoder");

        String geohashKey = "#g";
        String geohashValue1 = "2vghde";
        String geohashValue2 = "3abcde";
        String reqJsonWithCustomTagQueryFilterToDecode = "{\"" + geohashKey + "\":[\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]}";

        Filters decodedFilters = new FiltersDecoder<>().decode(reqJsonWithCustomTagQueryFilterToDecode);

        Filters expectedFilters = new Filters();
        List<String> expectedGeohashValuesList = List.of(geohashValue1, geohashValue2);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);

        assertEquals(expectedFilters, decodedFilters);
    }

    @Test
    public void testReqMessageDeserializer() throws JsonProcessingException {
        log.info("testReqMessageDeserializer");

        String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
        String geohashKey = "#g";
        String geohashValue = "2vghde";
        String reqJsonWithCustomTagQueryFilterToDecode = "[\"REQ\",\"" + subscriptionId + "\",{\"" + geohashKey + "\":[\"" + geohashValue + "\"]}]";

        ReqMessage decodedReqMessage = new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

        Filters expectedFilters = new Filters();
        List<String> expectedGeohashValuesList = List.of(geohashValue);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);

        ReqMessage expectedReqMessage = new ReqMessage(subscriptionId, expectedFilters);
        assertEquals(expectedReqMessage, decodedReqMessage);
    }

    @Test
    public void testReqMessageFilterListDecoder() throws JsonProcessingException {
        log.info("testReqMessageFilterListDecoder");

        String subscriptionId = "npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9";
        String geohashKey = "#g";
        String geohashValue1 = "2vghde";
        String geohashValue2 = "3abcde";
        String reqJsonWithCustomTagQueryFiltersToDecode = "[\"REQ\",\"" + subscriptionId + "\",{\"" + geohashKey + "\":[\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]}]";

        ReqMessage decodedReqMessage = new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFiltersToDecode);

        Filters expectedFilters = new Filters();
        List<String> expectedGeohashValuesList = List.of(geohashValue1, geohashValue2);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);

        ReqMessage expectedReqMessage = new ReqMessage(subscriptionId, expectedFilters);
        assertEquals(expectedReqMessage, decodedReqMessage);
    }

    @Test
    public void testReqMessagePopulatedFilterDecoder() throws JsonProcessingException {
        log.info("testReqMessagePopulatedFilterDecoder");

        String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
        String kind = "1";
        String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
        String geohashKey = "#g";
        String geohashValue1 = "2vghde";
        String geohashValue2 = "3abcde";
        String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
        String reqJsonWithCustomTagQueryFilterToDecode =
            "[\"REQ\", " +
                "\"" + subscriptionId + "\", " +
                "{\"kinds\": [" + kind + "], " +
                "\"authors\": [\"" + author + "\"]," +
                "\"" + geohashKey + "\": [\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]," +
                "\"#e\": [\"" + referencedEventId + "\"]}]";

        ReqMessage decodedReqMessage = new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

        Filters expectedFilters = new Filters();
        expectedFilters.setKinds(List.of(Kind.TEXT_NOTE));
        expectedFilters.setAuthors(List.of(new PublicKey(author)));
        expectedFilters.setReferencedEvents(List.of(new GenericEvent(referencedEventId)));
        List<String> expectedGeohashValuesList = List.of(geohashValue1, geohashValue2);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);

        ReqMessage expectedReqMessage = new ReqMessage(subscriptionId, expectedFilters);
        assertEquals(expectedReqMessage, decodedReqMessage);
    }

    @Test
    public void testReqMessagePopulatedListOfFiltersListDecoder() throws JsonProcessingException {
        log.info("testReqMessagePopulatedListOfFiltersListDecoder");

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
            "[\"REQ\", " +
                "\"" + subscriptionId + "\", " +
                "{\"kinds\": [" + kind + "], " +
                "\"authors\": [\"" + author + "\"]," +
                "\"" + geohashKey + "\": [\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]," +
                "\"" + uuidKey + "\": [\"" + uuidValue1 + "\",\"" + uuidValue2 + "\"]," +
                "\"#e\": [\"" + referencedEventId + "\"]}]";

        ReqMessage decodedReqMessage = new BaseMessageDecoder<ReqMessage>().decode(reqJsonWithCustomTagQueryFilterToDecode);

        Filters expectedFilters = new Filters();
        expectedFilters.setKinds(List.of(Kind.TEXT_NOTE));
        expectedFilters.setAuthors(List.of(new PublicKey(author)));
        expectedFilters.setReferencedEvents(List.of(new GenericEvent(referencedEventId)));
        List<String> expectedGeohashValuesList = List.of(geohashValue1, geohashValue2);
        expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);
        List<String> expectedIdentityTagValuesList = List.of(uuidValue1, uuidValue2);
        expectedFilters.setGenericTagQuery(uuidKey, expectedIdentityTagValuesList);
        ReqMessage expectedReqMessage = new ReqMessage(subscriptionId, expectedFilters);
        assertEquals(expectedReqMessage, decodedReqMessage);
    }
}
