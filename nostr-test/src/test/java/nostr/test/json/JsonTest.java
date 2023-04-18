package nostr.test.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.TagList;
import nostr.event.marshaller.impl.EventMarshaller;
import nostr.event.marshaller.impl.FiltersMarshaller;
import nostr.event.marshaller.impl.GenericTagQueryMarshaller;
import nostr.event.marshaller.impl.TagMarshaller;
import nostr.event.tag.DelegationTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.SubjectTag;
import nostr.id.Identity;
import nostr.json.parser.JsonParseException;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.json.unmarshaller.impl.JsonExpressionUnmarshaller;
import nostr.json.unmarshaller.impl.JsonNumberUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.json.unmarshaller.impl.JsonStringUnmarshaller;
import nostr.test.EntityFactory;
import nostr.types.Type;
import nostr.types.values.BaseValue;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.NumberValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@Log
public class JsonTest {

    @Test
    public void testParser() {
        System.out.println("testParser");

        StringValue jsonStr = new JsonStringUnmarshaller("\"34f\"").unmarshall();
        Assertions.assertEquals("34f", jsonStr.getValue().toString());

        NumberValue jsonNum = new JsonNumberUnmarshaller("46").unmarshall();
        Assertions.assertEquals(Integer.parseInt("46"), ((NumberValue) jsonNum).intValue());

        ArrayValue jsonArr = new JsonArrayUnmarshaller("[2,\"a\"]").unmarshall();
        Assertions.assertEquals(2, ((ArrayValue) jsonArr).length());
        Assertions.assertEquals(2, ((NumberValue) ((ArrayValue) jsonArr).get(0).get()).intValue());
        Assertions.assertEquals("\"a\"", ((ArrayValue) jsonArr).get(1).get().toString());

        jsonArr = new JsonArrayUnmarshaller("[1,2,\"bx\"]").unmarshall();
        Assertions.assertEquals(3, ((ArrayValue) jsonArr).length());
        Assertions.assertEquals(1, ((NumberValue) ((ArrayValue) jsonArr).get(0).get()).intValue());
        Assertions.assertEquals(2, ((NumberValue) ((ArrayValue) jsonArr).get(1).get()).intValue());
        Assertions.assertEquals("\"bx\"", ((ArrayValue) jsonArr).get(2).get().toString());

        jsonArr = new JsonArrayUnmarshaller("[2,\"a\",[1,2,\"bx\"]]").unmarshall();
        Assertions.assertEquals(3, ((ArrayValue) jsonArr).length());
        Assertions.assertTrue(((BaseValue) ((ArrayValue) jsonArr).get(2).get()).getType().equals(Type.ARRAY));

        jsonArr = new JsonArrayUnmarshaller("[2,\"a\",[1,2,\"bx\"],\"3\"   ,9]").unmarshall();
        Assertions.assertEquals(5, ((ArrayValue) jsonArr).length());

        jsonArr = new JsonArrayUnmarshaller("[[\"p\",\"\",\"null\",\"willy\"]]").unmarshall();
        Assertions.assertEquals(1, ((ArrayValue) jsonArr).length());

        jsonArr = new JsonArrayUnmarshaller("[[\"p\",\"\",\"null\",\"willy\"],[\"delegation\",\"\",\"whatever\",\"0d321c696337ffa923ea2d8fa40c04a326881063950eec26ce4eb7d06b7e84f78a9dd2a5ea267dfb1fba262568016b3bab533b7269c5b689922b3e157fcccdb9\"]]").unmarshall();
        Assertions.assertEquals(2, ((ArrayValue) jsonArr).length());

        IValue jsonObj = new JsonObjectUnmarshaller("{    \"a\":2,\"b\":\"a\"}").unmarshall();
        Assertions.assertTrue(((ObjectValue) jsonObj).getType().equals(Type.OBJECT));
        IValue v = ((ObjectValue) jsonObj).get("a").get();
        Assertions.assertTrue(((BaseValue) v).getType().equals(Type.NUMBER));
        Assertions.assertEquals(2, ((NumberValue) v).intValue());

        jsonArr = new JsonArrayUnmarshaller("[2,\"a\",[1,2,\"bx\", {\"a\":2,\"b\":\"a\"}]]").unmarshall();
        Assertions.assertEquals(3, ((ArrayValue) jsonArr).length());
        v = ((ArrayValue) jsonArr).get(2).get();
        Assertions.assertTrue(((BaseValue) v).getType().equals(Type.ARRAY));
        jsonObj = ((ArrayValue) v).get(3).get();
        Assertions.assertTrue(((ObjectValue) jsonObj).getType().equals(Type.OBJECT));

        jsonObj = new JsonObjectUnmarshaller("{\"a\":2,\"b\":\"a\", \"nil\":{}}").unmarshall();
        v = ((ObjectValue) jsonObj).get("nil").get();
        Assertions.assertTrue(((BaseValue) v).getType().equals(Type.OBJECT));

        Assertions.assertDoesNotThrow(
                () -> {
                    new JsonObjectUnmarshaller(("{\"tags\":[[\"p\",\"f6a04a16b1fb3b4bf40838dacc7f8bd4d46b60d3c9e2a4915877f9a2eac8e323\",\"nostr-java\"]],\"content\":\"Hello Astral, Please replace me!\",\"sig\":\"507b25e85fe42a2c6d2b67bca81f4c04e587448b4c3fc3ff0e6d3ee1ade5bad9758576e3e847d96082d38b389d0febac8d861b3c97534ca9b18afc0c2d4e2a15\",\"id\":\"d5e94446b140631740c7ada24cb8b01a4bb9f6c3c254e6ce61af0ce538968508\",\"kind\":1,\"pubkey\":\"f6a04a16b1fb3b4bf40838dacc7f8bd4d46b60d3c9e2a4915877f9a2eac8e323\",\"created_at\":1671152327}")).unmarshall();
                });
    }

    @Test
    public void testParserFail() {
        System.out.println("testParserFail");

        JsonParseException thrown = Assertions.assertThrows(JsonParseException.class,
                () -> {
                    new JsonStringUnmarshaller("\"34f").unmarshall();
                },
                "Parse error at position 4"
        );
        Assertions.assertNotNull(thrown);

        thrown = Assertions.assertThrows(JsonParseException.class,
                () -> {
                    new JsonObjectUnmarshaller("{    \"a\":2,\"b\"}").unmarshall();
                }
        );
        Assertions.assertNotNull(thrown);

        thrown = Assertions.assertThrows(JsonParseException.class,
                () -> {
                    new JsonObjectUnmarshaller("{\"a\":2,\"b\":\"a\", \"nil\":{}").unmarshall();
                }
        );
        Assertions.assertNotNull(thrown);
    }

    @Test
    public void testMarshalEvent() {
        try {
            System.out.println("testMarshalEvent");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(16);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            IEvent event = EntityFactory.Events.createTextNoteEvent(publicKey, "Free Willy!");

            Assertions.assertNotNull(new EventMarshaller(event, relay).marshall());
        } catch (IllegalArgumentException | UnsupportedNIPException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalEventWithOtsUnsupported() {
        try {
            System.out.println("testMarshalEventWithOtsUnsupported");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(16);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            IEvent event = EntityFactory.Events.createOtsEvent(publicKey);

            final String jsonEvent = new EventMarshaller(event, relay).marshall();

            log.log(Level.FINE, "jsonEvent: {0}", jsonEvent);

            Assertions.assertNotNull(jsonEvent);

            var jsonValue = new JsonObjectUnmarshaller(jsonEvent).unmarshall();

            NoSuchElementException thrown = Assertions.assertThrows(NoSuchElementException.class,
                    () -> {
                        ((ObjectValue) jsonValue).get("\"ots\"").get();
                    }
            );

            Assertions.assertNotNull(thrown);

        } catch (IllegalArgumentException | UnsupportedNIPException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalEventWithOtsSupported() {
        try {
            System.out.println("testMarshalEventWithOtsSupported");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(3);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            IEvent event = EntityFactory.Events.createOtsEvent(publicKey);
            //((GenericEvent) event).setOts(EntityFactory.generateRamdomAlpha(32));

            final String jsonEvent = new EventMarshaller(event, relay).marshall();

            log.log(Level.FINE, "jsonEvent: {0}", jsonEvent);

            Assertions.assertNotNull(jsonEvent);

            var jsonValue = new JsonObjectUnmarshaller(jsonEvent).unmarshall();

            Assertions.assertNotNull(((ObjectValue) jsonValue).get("ots"));

        } catch (IllegalArgumentException | UnsupportedNIPException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalEventFail() {
        try {
            System.out.println("testMarshalEventFail");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(5);
            supportedNips.add(16);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            IEvent event = EntityFactory.Events.createTextNoteEvent(publicKey, "Assange");

            UnsupportedNIPException thrown = Assertions.assertThrows(UnsupportedNIPException.class,
                    () -> {
                        new EventMarshaller(event, relay).marshall();
                    },
                    "This event is not supported. List of relay supported NIP(s): " + relay.printSupportedNips()
            );

            Assertions.assertNotNull(thrown);

        } catch (IllegalArgumentException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalTag() {
        try {
            System.out.println("testMarshalTag");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(16);
            supportedNips.add(26);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            TagList tags = new TagList();
            tags.add(PubKeyTag.builder().publicKey(publicKey).petName("willy").build());
            final DelegationTag delegationTag = new DelegationTag(publicKey, "whatever");
            Identity identity;
            identity = new Identity("/profile.properties");
            identity.sign(delegationTag);
            tags.add(delegationTag);

            IEvent event = new TextNoteEvent(publicKey, tags, "Free Willy!");

            final String jsonEvent = new EventMarshaller(event, relay).marshall();

            Assertions.assertNotNull(jsonEvent);

            var jsonValue = ((ObjectValue) new JsonObjectUnmarshaller(jsonEvent).unmarshall()).get("tags");

            var tagsArr = (ArrayValue) jsonValue.get();

            for (int i = 0; i < tagsArr.length(); i++) {
                var t = tagsArr.get(i).get();
                if (((ArrayValue) t).get(0).toString().equals("delegation")) {
                    Assertions.assertTrue(true);
                }
            }

            Assertions.assertFalse(false);

        } catch (IOException | NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalSubjectTag() {
        try {
            System.out.println("testMarshalSubjectTag");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(14);
            supportedNips.add(26);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            ITag subjectTag = new SubjectTag("Hello World!");

            var jsonSubjectTag = new TagMarshaller(subjectTag, relay).marshall();

            Assertions.assertNotNull(jsonSubjectTag);

            var jsonValue = ((ArrayValue) new JsonArrayUnmarshaller(jsonSubjectTag).unmarshall()).get(0);

            Assertions.assertEquals("\"subject\"", jsonValue.get().toString());
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalEventTag() {
        try {
            System.out.println("testMarshalEventTag");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(14);
            supportedNips.add(26);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});
            GenericEvent relatedEvent = EntityFactory.Events.createTextNoteEvent(publicKey);
            ITag eventTag = new EventTag(relatedEvent.getId());

            var jsonEventTag = new TagMarshaller(eventTag, relay).marshall();

            Assertions.assertNotNull(jsonEventTag);

            var jsonCodeValue = ((ArrayValue) new JsonArrayUnmarshaller(jsonEventTag).unmarshall()).get(0);
            //var jsonEventIdValue = ((JsonArrayValue) new JsonArrayUnmarshaller(jsonEventTag).unmarshall()).get(1);

            Assertions.assertEquals("\"e\"", jsonCodeValue.get().toString());
        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testMarshalTagFail() {
        try {
            System.out.println("testMarshalTagFail");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(100);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            TagList tags = new TagList();
            tags.add(PubKeyTag.builder().publicKey(publicKey).petName("willy").build());
            final DelegationTag delegationTag = new DelegationTag(publicKey, "whatever");
            Identity identity;
            identity = new Identity("/profile.properties");
            identity.sign(delegationTag);
            tags.add(delegationTag);

            IEvent event = new TextNoteEvent(publicKey, tags, "Free Willy!");

            UnsupportedNIPException thrown = Assertions.assertThrows(UnsupportedNIPException.class,
                    () -> {
                        new EventMarshaller(event, relay).marshall();
                    }
            );

            Assertions.assertNotNull(thrown);

        } catch (IOException | NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testGenericTagQueryMarshaller() {
        try {
            System.out.println("testGenericTagQuery");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(14);
            supportedNips.add(12);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            GenericTagQuery gtq = EntityFactory.Events.createGenericTagQuery();

            GenericTagQueryMarshaller gtqm = new GenericTagQueryMarshaller(gtq, relay);

            String strExpr = gtqm.marshall();

            IValue vexpr = new JsonExpressionUnmarshaller(strExpr).unmarshall();

            Assertions.assertTrue(vexpr instanceof ExpressionValue);

            ExpressionValue expr = (ExpressionValue) vexpr;

            String variable = "#" + gtq.getTagName().toString() + "";
            Assertions.assertEquals(variable, expr.getName());

            var jsonValue = expr.getValue();
            Assertions.assertTrue(jsonValue instanceof ArrayValue);

            var jsonArrValue = (ArrayValue) jsonValue;
            for (int i = 0; i < jsonArrValue.length(); i++) {
                var v = jsonArrValue.get(i).get().getValue().toString();
                Assertions.assertTrue(gtq.getValue().contains(v));
            }

        } catch (NostrException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testFilters() {
        try {
            System.out.println("testFilters");

            List<Integer> supportedNips = new ArrayList<>();
            supportedNips.add(1);
            supportedNips.add(5);
            supportedNips.add(14);
            supportedNips.add(12);

            Relay relay = Relay.builder().name("Free Domain").supportedNips(supportedNips).uri("ws://localhost:9999").build();

            PublicKey publicKey = new PublicKey(new byte[]{});

            Filters filters = EntityFactory.Events.createFilters(publicKey);

            var fm = new FiltersMarshaller(filters, relay);
            var strJson = fm.marshall();

            System.out.println("@@@ " + strJson);

            ObjectValue fObj = new JsonObjectUnmarshaller(strJson).unmarshall();

            ObjectValue obj = (ObjectValue) fObj;

            IValue ids = obj.get("ids").get();
            Assertions.assertNotNull(ids);
            Assertions.assertTrue(ids instanceof ArrayValue);
            Assertions.assertEquals(2, ((ArrayValue) ids).length());

            IValue e = obj.get("#e").get();
            Assertions.assertNotNull(e);
            Assertions.assertTrue(e instanceof ArrayValue);
            Assertions.assertEquals(1, ((ArrayValue) e).length());

            var gtql = filters.getGenericTagQueryList();
            Assertions.assertEquals(1, gtql.size());

            var c = gtql.getList().get(0).getTagName();
            var variable = "\"#" + c.toString() + "\"";
            Assertions.assertNotNull(obj.get(variable));

        } catch (UnsupportedNIPException ex) {
            Assertions.fail(ex);
        }
    }
}
