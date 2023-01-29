package nostr.test.event;

import nostr.crypto.bech32.Bech32;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.test.EntityFactory;
import java.io.IOException;
import java.util.Arrays;
import nostr.base.Bech32Prefix;
import nostr.base.ElementAttribute;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.Relay;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericMessage;
import nostr.event.impl.GenericTag;
import nostr.event.impl.TextNoteEvent;
import nostr.base.list.GenericTagQueryList;
import nostr.base.list.KindList;
import nostr.base.list.PublicKeyList;
import nostr.base.list.TagList;
import nostr.event.list.EventList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.TagListMarshaller;
import nostr.event.marshaller.impl.TagMarshaller;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.unmarshaller.impl.EventUnmarshaller;
import nostr.event.unmarshaller.impl.FiltersUnmarshaller;
import nostr.event.unmarshaller.impl.MessageUnmarshaller;
import nostr.event.unmarshaller.impl.TagListUnmarshaller;
import nostr.event.unmarshaller.impl.TagUnmarshaller;
import nostr.event.util.Nip05Validator;
import nostr.id.Client;
import nostr.json.parser.JsonParseException;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import nostr.util.UnsupportedNIPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class EventTest {

    private final Identity wallet;

    public EventTest() throws IOException, NostrException {
        this.wallet = new Identity();
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

            IValue tags = ((ObjectValue) jsonValue).get("\"tags\"").get();

            Assertions.assertEquals(2, ((ArrayValue) tags).length());

            IValue tag = ((ArrayValue) tags).get(1).get();

            Assertions.assertTrue(tag instanceof ArrayValue);

            IValue code = ((ArrayValue) tag).get(0).get();

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

            IValue tags = ((ObjectValue) jsonValue).get("\"tags\"").get();

            Assertions.assertEquals(2, ((ArrayValue) tags).length());

            IValue tag = ((ArrayValue) tags).get(1).get();

            Assertions.assertTrue(tag instanceof ArrayValue);

            IValue code = ((ArrayValue) tag).get(0).get();

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

    @Test
    public void testUnmarshallEvent() throws NostrException {
        System.out.println("testUnmarshallEvent");

        // Tag
        PublicKey publicKey = this.wallet.getProfile().getPublicKey();
        var tag = PubKeyTag.builder().publicKey(publicKey).petName("john").build();
        var unTag = new TagUnmarshaller(new TagMarshaller(tag, null).marshall()).unmarshall();
        Assertions.assertEquals(tag.getCode(), unTag.getCode());
        //Assertions.assertEquals(tag.getPetName(), ((GenericTag)unTag).getAttributes().);

        // TagList
        var tagList = new TagList();
        tagList.add(tag);
        tagList.add(new NonceTag(Integer.SIZE, Integer.MIN_VALUE));
        var unTagList = new TagListUnmarshaller(new TagListMarshaller(tagList, null).marshall()).unmarshall();
        Assertions.assertEquals(tagList.size(), unTagList.size());

        // Event
        var event = EntityFactory.Events.createOtsEvent(publicKey);
        var unmarshalledEvent = new EventUnmarshaller(new EventMarshaller(event, null).marshall()).unmarshall();
        Assertions.assertEquals(event.getKind(), ((GenericEvent) unmarshalledEvent).getKind());

        // Filters
        var authors = new PublicKeyList();
        authors.add(publicKey);
        var kindList = new KindList();
        kindList.add(Kind.DELETION);
        kindList.add(Kind.ENCRYPTED_DIRECT_MESSAGE);
        var filters = EntityFactory.Events.createFilters(authors, kindList, Long.MIN_VALUE);
        EventList eventList = new EventList();
        final TextNoteEvent evt = EntityFactory.Events.createTextNoteEvent(publicKey);
        eventList.add(evt);
        filters.setEvents(eventList);
        Filters unFilters = (Filters) new FiltersUnmarshaller(filters.toString()).unmarshall();
        Assertions.assertEquals(filters.getKinds().size(), unFilters.getKinds().size());
        Assertions.assertTrue(unFilters.getKinds().getList().contains(Kind.DELETION));
        Assertions.assertTrue(unFilters.getKinds().getList().contains(Kind.ENCRYPTED_DIRECT_MESSAGE));
        Assertions.assertTrue(unFilters.getAuthors().getList().contains(publicKey));
        Assertions.assertEquals(1, unFilters.getEvents().getList().size());
        Assertions.assertTrue(unFilters.getEvents().getList().get(0).getId().equals(evt.getId()));

        // Filters with GenericTagQueryList
        var gtqList = new GenericTagQueryList();
        final GenericTagQuery gtq = GenericTagQuery.builder().tagName('x').value(Arrays.asList("one", "two", "three")).build();
        gtqList.add(gtq);
        filters.setGenericTagQueryList(gtqList);
        unFilters = (Filters) new FiltersUnmarshaller(filters.toString()).unmarshall();
        Assertions.assertTrue(unFilters.getGenericTagQueryList().getList().contains(gtq));
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
        msg.addAttribute(new ElementAttribute(attr));

        var mu = new MessageUnmarshaller("[\"AUTH\", \"challenge-string\"]");
        var um = mu.unmarshall();

        Assertions.assertEquals(msg.getCommand(), um.getCommand());
        Assertions.assertEquals(1, um.getAttributes().size());

        var muattr = ((StringValue) msg.getAttributes().iterator().next().getValue()).getValue().toString();
        Assertions.assertEquals(attr, muattr);
    }
}
