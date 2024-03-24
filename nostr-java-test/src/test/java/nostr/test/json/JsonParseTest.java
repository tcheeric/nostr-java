package nostr.test.json;

import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.list.EventList;
import nostr.event.list.FiltersList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertEquals(Command.REQ.toString(), message.getCommand());
        Assertions.assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", ((ReqMessage) message).getSubscriptionId());
        Assertions.assertEquals(1, ((ReqMessage) message).getFiltersList().size());

        var filters = ((ReqMessage) message).getFiltersList().getList().get(0);

        Assertions.assertEquals(1, filters.getKinds().size());
        Assertions.assertEquals(1, filters.getKinds().getList().get(0).intValue());

        Assertions.assertEquals(1, filters.getAuthors().size());
        Assertions.assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", filters.getAuthors().getList().get(0).toBech32String());

        Assertions.assertEquals(1, filters.getReferencedEvents().size());
        Assertions.assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", filters.getReferencedEvents().getList().get(0).getId());
    }

    @Test
    public void testBaseReqMessageEncoder() {
        System.out.println("testBaseReqMessageEncoder");

        final var filtersList = new FiltersList();
        var publicKey = Identity.generateRandomIdentity().getPublicKey();
        filtersList.add(Filters.builder().authors(new PublicKeyList(publicKey)).kinds(new KindList(3,5)).build());
        filtersList.add(Filters.builder().kinds(new KindList(0,1)).build());
        filtersList.add(Filters.builder().referencedEvents(new EventList(new BaseEvent.ProxyEvent("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712"))).build());

        final var reqMessage = new ReqMessage(publicKey.toString(), filtersList);

        BaseMessageEncoder encoder = new BaseMessageEncoder(reqMessage);

        var jsonMessage = encoder.encode();

        var message = new BaseMessageDecoder(jsonMessage).decode();

        Assertions.assertEquals(reqMessage, message);
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

        Assertions.assertEquals(Command.EVENT.toString(), message.getCommand());

        final var event = (GenericEvent) (((EventMessage) message).getEvent());
        Assertions.assertEquals("npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh", ((EventMessage) message).getSubscriptionId());
        Assertions.assertEquals(1, event.getKind().intValue());
        Assertions.assertEquals(1686199583, event.getCreatedAt().longValue());
        Assertions.assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());
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
                Assertions.assertEquals(Marker.ROOT, et.getMarker());
            }
        }
    }

    @Test
    public void testGenericTagDecoder() {
        System.out.println("testGenericTagDecoder");
        final String jsonString = "[\"saturn\", \"jetpack\", false]";

        var tag = new GenericTagDecoder(jsonString).decode();

        Assertions.assertEquals("saturn", tag.getCode());
        Assertions.assertEquals(2, tag.getAttributes().size());
        Assertions.assertEquals("jetpack", ((ElementAttribute) (tag.getAttributes().toArray())[0]).getValue());
        Assertions.assertEquals(false, Boolean.valueOf(((ElementAttribute) (tag.getAttributes().toArray())[1]).getValue().toString()));
    }

    @Test
    public void testDeserializeTag() throws NostrException {
        System.out.println("testDeserializeTag");

        String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
        final String jsonString = "[\"p\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
        var tag = new BaseTagDecoder(jsonString).decode();

        Assertions.assertTrue(tag instanceof PubKeyTag);

        PubKeyTag pTag = (PubKeyTag) tag;
        Assertions.assertEquals("wss://nostr.java", pTag.getMainRelayUrl());
        Assertions.assertEquals(npubHex, pTag.getPublicKey().toString());
        Assertions.assertEquals("alice", pTag.getPetName());
    }

    @Test
    public void testDeserializeGenericTag() throws NostrException {
        System.out.println("testDeserializeGenericTag");

        String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
        final String jsonString = "[\"gt\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
        var tag = new BaseTagDecoder(jsonString).decode();

        Assertions.assertTrue(tag instanceof GenericTag);

        GenericTag gTag = (GenericTag) tag;
        Assertions.assertEquals("gt", gTag.getCode());
    }
}
