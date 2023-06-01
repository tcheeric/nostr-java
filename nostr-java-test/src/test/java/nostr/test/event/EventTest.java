package nostr.test.event;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nostr.base.ElementAttribute;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.impl.GenericTag;
import nostr.event.marshaller.impl.ElementMarshaller;
import nostr.event.util.Nip05Validator;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
public class EventTest {

    //private final Identity identity;
    public EventTest() throws IOException, NostrException {
    }

    @Test
    public void testCreateTextNoteEvent() throws NostrException {
        System.out.println("testCreateTextNoteEvent");
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        instance.update();
        Assertions.assertNotNull(instance.getId());
        Assertions.assertNotNull(instance.getCreatedAt());
        Assertions.assertNull(instance.getSignature());
        final String bech32 = instance.toBech32();
        Assertions.assertNotNull(bech32);
        Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
    }

    @Test
    public void testCreateGenericTag() {
//        try {
//            System.out.println("testCreateGenericTag");
//            PublicKey publicKey = this.identity.getPublicKey();
//            GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);
//
//            Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
//            relay.addNipSupport(1);
//            relay.addNipSupport(genericTag.getNip());
//            var attrs = genericTag.getAttributes();
//            for (var a : attrs) {
//                relay.addNipSupport(a.getNip());
//            }
//
//            ElementMarshaller marshaller = new ElementMarshaller(genericTag.getParent(), relay);
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
//
//        } catch (NostrException ex) {
//            Assertions.fail(ex);
//        }
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
//            ElementMarshaller marshaller = new ElementMarshaller(genericTag.getParent(), relay);
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

    @Test
    public void testCreateUnsupportedGenericTag() throws IOException, NostrException {
        System.out.println("testCreateUnsupportedGenericTag");
        //PublicKey publicKey = this.identity.getPublicKey();
        PublicKey publicKey = Identity.getInstance().getPublicKey();
        IEvent event = EntityFactory.Events.createOtsEvent(publicKey);
        GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey, event, 7);

        Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
        relay.addNipSupport(0);

        ElementMarshaller marshaller = new ElementMarshaller(genericTag.getParent(), relay);

        UnsupportedNIPException thrown = Assertions.assertThrows(UnsupportedNIPException.class,
                () -> {
                    marshaller.marshall();
                },
                "This event is not supported. List of relay supported NIP(s): " + relay.printSupportedNips()
        );

        Assertions.assertNotNull(thrown);
    }

    @Test
    public void testNip05Validator() {
        System.out.println("testNip05Validator");
        try {
            var nip05 = "erict875@getalby.com";
            var publicKey = new PublicKey(NostrUtil.hexToBytes(Bech32.fromBech32("npub126klq89p42wk78p4j5ur8wlxmxdqepdh8tez9e4axpd4run5nahsmff27j")));

            Nip05Validator nip05Validator = Nip05Validator.builder().nip05(nip05).publicKey(publicKey).build();

            nip05Validator.validate();
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
        Assertions.assertTrue(true);
    }

    @Test
    public void testAuthMessage() {
        System.out.println("testAuthMessage");

        GenericMessage msg = new GenericMessage("AUTH", 42);
        String attr = "challenge-string";
        msg.addAttribute(ElementAttribute.builder().name("challenge").value(attr).build());

        var muattr = (msg.getAttributes().iterator().next().getValue()).toString();
        Assertions.assertEquals(attr, muattr);
    }
}
