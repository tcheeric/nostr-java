package nostr.test.event;

import nostr.crypto.bech32.Bech32;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Wallet;
import nostr.test.EntityFactory;
import java.io.IOException;
import nostr.base.Bech32Prefix;
import nostr.base.IEvent;
import nostr.base.Relay;
import nostr.event.impl.GenericTag;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class EventTest {

    private final Wallet wallet;

    public EventTest() throws IOException, NostrException {
        this.wallet = new Wallet();
    }

    @Test
    public void testCreateTextNoteEvent() {
        try {
            System.out.println("testCreateTextNoteEvent");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
            Assertions.assertNotNull(instance.getId());
            Assertions.assertNotNull(instance.getCreatedAt());
            Assertions.assertNull(instance.getSignature());
            final String bech32 = instance.toBech32();
            Assertions.assertNotNull(bech32);
            Assertions.assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testCreateGenericTag() {
        try {
            System.out.println("testCreateGenericTag");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);

            Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
            relay.addNipSupport(1);
            relay.addNipSupport(genericTag.getNip());
            var attrs = genericTag.getAttributes();
            for (var a : attrs) {
                relay.addNipSupport(a.getNip());
            }

            EventMarshaller marshaller = new EventMarshaller(genericTag.getParent(), relay);
            var strJsonEvent = marshaller.marshall();

            var jsonValue = new JsonObjectUnmarshaller(strJsonEvent).unmarshall();

            IValue tags = ((ObjectValue) jsonValue).get("\"tags\"");

            Assertions.assertEquals(2, ((ArrayValue) tags).length());

            IValue tag = ((ArrayValue) tags).get(1);

            Assertions.assertTrue(tag instanceof ArrayValue);

            IValue code = ((ArrayValue) tag).get(0);

            Assertions.assertTrue(code instanceof StringValue);

            Assertions.assertEquals("devil", code.getValue());

        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testCreateUnsupportedGenericTagAttribute() {
        try {
            System.out.println("testCreateUnsupportedGenericTagAttribute");
            PublicKey publicKey = this.wallet.getProfile().getPublicKey();
            GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);

            Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
            relay.addNipSupport(1);
            relay.addNipSupport(genericTag.getNip());

            EventMarshaller marshaller = new EventMarshaller(genericTag.getParent(), relay);
            var strJsonEvent = marshaller.marshall();

            var jsonValue = new JsonObjectUnmarshaller(strJsonEvent).unmarshall();

            IValue tags = ((ObjectValue) jsonValue).get("\"tags\"");

            Assertions.assertEquals(2, ((ArrayValue) tags).length());

            IValue tag = ((ArrayValue) tags).get(1);

            Assertions.assertTrue(tag instanceof ArrayValue);

            IValue code = ((ArrayValue) tag).get(0);

            Assertions.assertTrue(code instanceof StringValue);

            Assertions.assertEquals("devil", code.getValue());
            Assertions.assertEquals(1, ((ArrayValue) tag).length());

        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testCreateUnsupportedGenericTag() {
        System.out.println("testCreateUnsupportedGenericTag");
        PublicKey publicKey = this.wallet.getProfile().getPublicKey();
        IEvent event = EntityFactory.Events.createOtsEvent(publicKey);
        GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey, event, 7);

        Relay relay = Relay.builder().uri("wss://secret.relay.com").build();
        relay.addNipSupport(0);

        EventMarshaller marshaller = new EventMarshaller(genericTag.getParent(), relay);

        UnsupportedNIPException thrown = Assertions.assertThrows(UnsupportedNIPException.class,
                () -> {
                    marshaller.marshall();
                },
                "This event is not supported. List of relay supported NIP(s): " + relay.printSupportedNips()
        );

        Assertions.assertNotNull(thrown);
    }
}
