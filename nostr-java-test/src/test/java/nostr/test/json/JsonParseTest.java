package nostr.test.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.api.NIP01;
import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.BaseEvent;
import nostr.event.BaseEvent.ProxyEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Marker;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author eric
 */
public class JsonParseTest {

    @Test
    public void testBaseReqMessageDecoder() {
        System.out.println("testBaseReqMessageDecoder");

        final String parseTarget =
            "[\"REQ\", " +
                "\"npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh\", " +
                "{\"kinds\": [1], " +
                "\"authors\": [\"f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75\"]," +
                "\"#e\": [\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\"]}]";

        final var message = new BaseMessageDecoder(parseTarget).decode();

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
    public void testBaseReqMessageEncoder() {
        System.out.println("testBaseReqMessageEncoder");

        final var filtersList = new ArrayList<Filters>();
        var publicKey = Identity.generateRandomIdentity().getPublicKey();
        filtersList.add(Filters.builder().authors(new ArrayList<>(List.of(publicKey))).kinds(new ArrayList<>(List.of(Kind.CONTACT_LIST, Kind.DELETION))).build());
        filtersList.add(Filters.builder().kinds(new ArrayList<>(List.of(Kind.SET_METADATA, Kind.TEXT_NOTE))).build());

        filtersList.add(Filters.builder().referencedEvents(new ArrayList<>(List.of(new ProxyEvent("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712")))).build());

        final var reqMessage = new ReqMessage(publicKey.toString(), filtersList);

        BaseMessageEncoder encoder = new BaseMessageEncoder(reqMessage);

        var jsonMessage = encoder.encode();

        var jsonMsg = jsonMessage.substring(1, jsonMessage.length() - 1);
        var parts = jsonMsg.split(",");
        Assertions.assertEquals("\"REQ\"", parts[0]);
        Assertions.assertEquals("\"" + publicKey.toString() + "\"", parts[1]);
        Assertions.assertFalse(parts[2].startsWith("["));
        Assertions.assertFalse(parts[parts.length - 1].endsWith("]"));

        var message = new BaseMessageDecoder(jsonMessage).decode();

        assertEquals(reqMessage, message);
    }

    @Test
    public void testBaseEventMessageDecoder() {
        System.out.println("testBaseEventMessageDecoder");

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

        final var message = new BaseMessageDecoder(parseTarget).decode();

        assertEquals(Command.EVENT.toString(), message.getCommand());

        final var event = (GenericEvent) (((EventMessage) message).getEvent());
        assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", ((EventMessage) message).getSubscriptionId());
        assertEquals(1, event.getKind().intValue());
        assertEquals(1686199583, event.getCreatedAt().longValue());
        assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());
    }

    @Test
    public void testBaseEventMessageMarkerDecoder() {
        System.out.println("testBaseEventMessageMarkerDecoder");

        String json = "["
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

        BaseMessageDecoder decoder = new BaseMessageDecoder(json);
        BaseMessage message = decoder.decode();

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
        System.out.println("testGenericTagDecoder");
        final String jsonString = "[\"saturn\", \"jetpack\", false]";

        var tag = new GenericTagDecoder(jsonString).decode();

        assertEquals("saturn", tag.getCode());
        assertEquals(2, tag.getAttributes().size());
        assertEquals("jetpack", ((ElementAttribute) (tag.getAttributes().toArray())[0]).getValue());
        assertEquals(false, Boolean.valueOf(((ElementAttribute) (tag.getAttributes().toArray())[1]).getValue().toString()));
    }

    @Test
    public void testClassifiedListingTagSerializer() throws NostrException, JsonProcessingException {
        System.out.println("testClassifiedListingSerializer");
        var classifiedListingEventJson = "{"
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

        GenericEvent event = new GenericEventDecoder<>(classifiedListingEventJson).decode();
        EventMessage message = NIP01.createEventMessage(event, "1");
        assertEquals(1, message.getNip());
        assertEquals("{\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\",\"kind\":30402,\"content\":\"content ipsum\",\"pubkey\":\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\",\"created_at\":1687765220,\"tags\":[[\"p\",\"ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de\"],[\"title\",\"title ipsum\"],[\"summary\",\"summary ipsum\"],[\"published_at\",\"1687765220\"],[\"location\",\"location ipsum\"],[\"price\",\"11111\",\"BTC\",\"1\"]],\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}", new BaseEventEncoder<>((BaseEvent) message.getEvent()).encode());

        assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", event.getId());
        assertEquals(30402, event.getKind());
        assertEquals("content ipsum", event.getContent());
        assertEquals("ec0762fe78b0f0b763d1324452d973a38bef576d1d76662722d2b8ff948af1de", event.getPubKey().toString());
        assertEquals(1687765220L, event.getCreatedAt());
        assertEquals("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546", event.getSignature().toString());

        List<GenericTag> genericTags = event.getTags().stream()
            .filter(GenericTag.class::isInstance)
            .map(GenericTag.class::cast).toList();

        List<ElementAttribute> price = genericTags.stream()
            .filter(tag -> tag.getCode().equalsIgnoreCase("price")).map(GenericTag::getAttributes).toList().get(0);
        assertEquals("11111", price.get(0).getValue());
        assertEquals("BTC", price.get(1).getValue());
        assertEquals("1", price.get(2).getValue());

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
    public void testDeserializeTag() throws NostrException {
        System.out.println("testDeserializeTag");

        String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
        final String jsonString = "[\"p\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
        var tag = new BaseTagDecoder(jsonString).decode();

        Assertions.assertTrue(tag instanceof PubKeyTag);

        PubKeyTag pTag = (PubKeyTag) tag;
        assertEquals("wss://nostr.java", pTag.getMainRelayUrl());
        assertEquals(npubHex, pTag.getPublicKey().toString());
        assertEquals("alice", pTag.getPetName());
    }

    @Test
    public void testDeserializeGenericTag() throws NostrException {
        System.out.println("testDeserializeGenericTag");

        String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
        final String jsonString = "[\"gt\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
        var tag = new BaseTagDecoder(jsonString).decode();

        Assertions.assertTrue(tag instanceof GenericTag);

        GenericTag gTag = (GenericTag) tag;
        assertEquals("gt", gTag.getCode());
    }

    @Test
    public void testFiltersEncoder() {
        System.out.println("testFiltersEncoder");

        String new_geohash = "2vghde";
        List<String> geohashList = new ArrayList<>();
        geohashList.add(new_geohash);
        GenericTagQuery genericTagQuery = new GenericTagQuery();
        genericTagQuery.setTagName("g");
        genericTagQuery.setValue(geohashList);
        Filters filters = Filters.builder().genericTagQuery(genericTagQuery).build();

        FiltersEncoder encoder = new FiltersEncoder(filters);
        String jsonMessage = encoder.encode();
        assertEquals("{\"#g\":[\"2vghde\"]}", jsonMessage);
    }

    @Test
    public void testReqMessageSerializer() {
        System.out.println("testFiltersEncoder");

        String new_geohash = "2vghde";
        List<String> geohashList = new ArrayList<>();
        geohashList.add(new_geohash);
        GenericTagQuery genericTagQuery = new GenericTagQuery();
        genericTagQuery.setTagName("g");
        genericTagQuery.setValue(geohashList);
        Filters filters = Filters.builder().genericTagQuery(genericTagQuery).build();

        ReqMessage reqMessage = new ReqMessage("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9", new ArrayList<Filters>(List.of(filters)));
        BaseMessageEncoder encoder = new BaseMessageEncoder(reqMessage);
        String jsonMessage = encoder.encode();

        assertEquals("[\"REQ\",\"npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9\",{\"#g\":[\"2vghde\"]}]", jsonMessage);
    }
}
