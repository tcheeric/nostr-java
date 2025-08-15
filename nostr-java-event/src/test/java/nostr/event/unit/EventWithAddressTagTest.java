package nostr.event.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public class EventWithAddressTagTest {
    @Test
    public void decodeTestWithRelay() throws JsonProcessingException {

        String json = "["
            + "\"EVENT\","
            + "{"
            + "\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\","
            + "\"kind\":1,"
            + "\"pubkey\":\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\","
            + "\"created_at\":1687765220,"
            + "\"content\":\"手順書が間違ってたら作業者は無理だな\","
            + "\"tags\":["
            + "[\"a\",\"1:f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75:UUID-1\",\"ws://localhost:8080\"]"
            + "],"
            + "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\""
            + "}]";

        BaseMessage message = new BaseMessageDecoder<>().decode(json);

        assertEquals("EVENT", message.getCommand());
        assertInstanceOf(EventMessage.class, message);

        EventMessage eventMessage = (EventMessage) message;

        GenericEvent eventImpl = (GenericEvent) eventMessage.getEvent();

        assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", eventImpl.getId());
        assertEquals(1, eventImpl.getKind());
        assertEquals("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984", eventImpl.getPubKey().toString());
        assertEquals(1687765220, eventImpl.getCreatedAt());
        assertEquals("手順書が間違ってたら作業者は無理だな", eventImpl.getContent());
        assertEquals("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546",
            eventImpl.getSignature().toString());

        List<BaseTag> expectedTags = new ArrayList<>();

        Integer kind = 1;
        String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
        PublicKey publicKey = new PublicKey(author);
        IdentifierTag identifierTag = new IdentifierTag("UUID-1");
        Relay relay = new Relay("ws://localhost:8080");

        AddressTag addressTag = new AddressTag();
        addressTag.setKind(kind);
        addressTag.setPublicKey(publicKey);
        addressTag.setIdentifierTag(identifierTag);
        addressTag.setRelay(relay);
        expectedTags.add(addressTag);

        List<? extends BaseTag> actualTags = eventImpl.getTags();

        for (int i = 0; i < expectedTags.size(); i++) {
            BaseTag expected = expectedTags.get(i);
            if (expected instanceof AddressTag expectedAddressTag) {
                AddressTag actualAddressTag = (AddressTag) actualTags.get(i);
                assertEquals(expectedAddressTag.getKind(), actualAddressTag.getKind());
                assertEquals(expectedAddressTag.getPublicKey(), actualAddressTag.getPublicKey());
                assertEquals(expectedAddressTag.getIdentifierTag(), actualAddressTag.getIdentifierTag());
                assertEquals(expectedAddressTag.getRelay(), actualAddressTag.getRelay());
            } else {
                fail();
            }
        }
    }

    @Test
    public void decodeTestWithoutRelay() throws JsonProcessingException {

        String json = "["
            + "\"EVENT\","
            + "{"
            + "\"id\":\"28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a\","
            + "\"kind\":1,"
            + "\"pubkey\":\"2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984\","
            + "\"created_at\":1687765220,"
            + "\"content\":\"手順書が間違ってたら作業者は無理だな\","
            + "\"tags\":["
            + "[\"a\",\"1:f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75:UUID-1\"]"
            + "],"
            + "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\""
            + "}]";

        BaseMessage message = new BaseMessageDecoder<>().decode(json);

        assertEquals("EVENT", message.getCommand());
        assertInstanceOf(EventMessage.class, message);

        EventMessage eventMessage = (EventMessage) message;

        GenericEvent eventImpl = (GenericEvent) eventMessage.getEvent();

        assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", eventImpl.getId());
        assertEquals(1, eventImpl.getKind());
        assertEquals("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984", eventImpl.getPubKey().toString());
        assertEquals(1687765220, eventImpl.getCreatedAt());
        assertEquals("手順書が間違ってたら作業者は無理だな", eventImpl.getContent());
        assertEquals("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546",
            eventImpl.getSignature().toString());

        List<BaseTag> expectedTags = new ArrayList<>();

        Integer kind = 1;
        String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
        PublicKey publicKey = new PublicKey(author);
        IdentifierTag identifierTag = new IdentifierTag("UUID-1");

        AddressTag addressTag = new AddressTag();
        addressTag.setKind(kind);
        addressTag.setPublicKey(publicKey);
        addressTag.setIdentifierTag(identifierTag);
        expectedTags.add(addressTag);

        List<? extends BaseTag> actualTags = eventImpl.getTags();

        for (int i = 0; i < expectedTags.size(); i++) {
            BaseTag expected = expectedTags.get(i);
            if (expected instanceof AddressTag expectedAddressTag) {
                AddressTag actualAddressTag = (AddressTag) actualTags.get(i);
                assertEquals(expectedAddressTag.getKind(), actualAddressTag.getKind());
                assertEquals(expectedAddressTag.getPublicKey(), actualAddressTag.getPublicKey());
                assertEquals(expectedAddressTag.getIdentifierTag(), actualAddressTag.getIdentifierTag());
            } else {
                fail();
            }
        }
    }
}
