package nostr.id;

import lombok.extern.java.Log;
import nostr.base.ElementAttribute;

import org.junit.jupiter.api.Test;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.util.Nip05Validator;
import nostr.util.NostrUtil;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author squirrel
 */
@Log
public class EventTest {

    public EventTest() {
    }

    @Test
    public void testCreateTextNoteEvent(){
        log.info("testCreateTextNoteEvent");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        instance.update();
        assertNotNull(instance.getId());
        assertNotNull(instance.getCreatedAt());
        assertNull(instance.getSignature());
        final String bech32 = instance.toBech32();
        assertNotNull(bech32);
        assertDoesNotThrow(() -> {
            assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
        });
    }

    @Test
    public void testCreateGenericTag() {
        log.info("testCreateGenericTag");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);

        Relay relay = new Relay("wss://secret.relay.com");
        relay.addNipSupport(1);
        relay.addNipSupport(genericTag.getNip());

        var encoder = new BaseTagEncoder(genericTag, relay);
        var strJsonEvent = encoder.encode();

        assertDoesNotThrow(() -> {
            BaseTag tag = ENCODER_MAPPED_AFTERBURNER.readValue(strJsonEvent, BaseTag.class);
            assertEquals(genericTag, tag);
        });
    }

    @Test
    public void testCreateUnsupportedGenericTagAttribute() {
//        try {
//            System.out.println("testCreateUnsupportedGenericTagAttribute");
//            PublicKey publicKey = this.identity.getPublicKey();
//            GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);
//
//            Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
//            relay.addNipSupport(1);
//            relay.addNipSupport(genericTag.getNip());
//
//            BaseEventEncoder marshaller = new BaseEventEncoder(genericTag.getParent(), relay);
//            var strJsonEvent = marshaller.marshall();
//
//            var jsonValue = new JsonObjectUnmarshaller(strJsonEvent).unmarshall();
//
//            IValue tags = ((ObjectValue) jsonValue).get("tags").get();
//
//            Assertions.assertEquals(2, ((ArrayValue) tags).length());
//
//            IValue tag = ((ArrayValue) tags).get(1).get();
//
//            Assertions.assertTrue(tag instanceof ArrayValue);
//
//            IValue code = ((ArrayValue) tag).get(0).get();
//
//            Assertions.assertTrue(code instanceof StringValue);
//
//            Assertions.assertEquals("devil", code.getValue());
//            Assertions.assertEquals(1, ((ArrayValue) tag).length());
//
//        } catch (NostrException ex) {
//            Assertions.fail(ex);
//        }
    }

    // TODO - Rewrite the test class after implementing the configuration
/*
    @Test
    public void testCreateUnsupportedGenericTag() {
        System.out.println("testCreateUnsupportedGenericTag");
        //PublicKey publicKey = this.identity.getPublicKey();
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        IEvent event = EntityFactory.Events.createOtsEvent(publicKey);
        GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey, event, 7);

        Relay relay = new Relay("wss://secret.relay.com");
        relay.addNipSupport(0);

        var encoder = new BaseEventEncoder((BaseEvent) genericTag.getParent(), relay);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
                () -> {
                    encoder.encode();
                },
                "This event is not supported. List of relay supported NIP(s): " + relay.printSupportedNips()
        );

        Assertions.assertNotNull(thrown);
    }
*/

    @Test
    public void testNip05Validator() {
        System.out.println("testNip05Validator");
        try {
            var nip05 = "nostr-java@nostr.band";
            var publicKey = new PublicKey(NostrUtil.hexToBytes(Bech32.fromBech32("npub126klq89p42wk78p4j5ur8wlxmxdqepdh8tez9e4axpd4run5nahsmff27j")));

            var nip05Validator = Nip05Validator.builder().nip05(nip05).publicKey(publicKey).build();

            nip05Validator.validate();
        } catch (Exception ex) {
            fail(ex);
        }
        assertTrue(true);
    }

    @Test
    public void testAuthMessage() {
        System.out.println("testAuthMessage");

        GenericMessage msg = new GenericMessage("AUTH", 42);
        String attr = "challenge-string";
        msg.addAttribute(ElementAttribute.builder().name("challenge").value(attr).build());

        var muattr = (msg.getAttributes().getFirst().getValue()).toString();
        assertEquals(attr, muattr);
    }

    @Test
    public void testEventIdConstraints() {
        log.info("testCreateTextNoteEvent");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericEvent genericEvent = EntityFactory.Events.createTextNoteEvent(publicKey);
        String id64chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
        assertDoesNotThrow(() -> genericEvent.setId(id64chars));

        String id63chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a71";
        assertTrue(
            assertThrows(AssertionError.class, () -> genericEvent.setId(id63chars))
                .getMessage().contains("[fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a71], length: [63], target length: [64]"));

        String id65chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a7123";
        assertTrue(
            assertThrows(AssertionError.class, () -> genericEvent.setId(id65chars))
                .getMessage().contains("[fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a7123], length: [65], target length: [64]"));
    }
}
