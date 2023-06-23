package nostr.test.json;

import nostr.base.Command;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericTagDecoder;
import nostr.event.json.codec.TagDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.PubKeyTag;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author eric
 */
public class JsonParseTest {

    @Test
    public void issue39() throws NostrException {
        System.out.println("issue39");

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
    public void testGenericTagDecoder() throws NostrException {
        System.out.println("testGenericTagDecoder");
        final String jsonString = "[\"saturn\", \"jetpack\", false]";

        var tag = new GenericTagDecoder(jsonString).decode();

        Assertions.assertEquals("saturn", tag.getCode());
        Assertions.assertEquals(2, tag.getAttributes().size());
        Assertions.assertEquals("jetpack", ((ElementAttribute) (tag.getAttributes().toArray())[1]).getValue());
        Assertions.assertEquals(false, Boolean.valueOf(((ElementAttribute) (tag.getAttributes().toArray())[1]).getValue().toString()));
    }

    @Test
    public void testDeserializeTag() throws NostrException {
        System.out.println("testDeserializeTag");

        String npubHex = new PublicKey(Bech32.decode("npub1clk6vc9xhjp8q5cws262wuf2eh4zuvwupft03hy4ttqqnm7e0jrq3upup9").data).toString();
        final String jsonString = "[\"p\", \"" + npubHex + "\", \"wss://nostr.java\", \"alice\"]";
        var tag = new TagDecoder(jsonString).decode();

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
        var tag = new TagDecoder(jsonString).decode();

        Assertions.assertTrue(tag instanceof GenericTag);

        GenericTag gTag = (GenericTag) tag;
        Assertions.assertEquals("gt", gTag.getCode());
    }
}
