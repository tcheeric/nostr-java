package nostr.test.event;

import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import nostr.event.BaseMessage;

public class DecodeTest {

    @Test
    public void decodeTest() throws NostrException {

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

        Assertions.assertEquals("EVENT", message.getCommand());
        EventMessage eventMessage = (EventMessage) message;

        Assertions.assertEquals("temp20230627", eventMessage.getSubscriptionId());
        GenericEvent eventImpl = (GenericEvent) eventMessage.getEvent();

        Assertions.assertEquals("28f2fc030e335d061f0b9d03ce0e2c7d1253e6fadb15d89bd47379a96b2c861a", eventImpl.getId());
        Assertions.assertEquals(1, eventImpl.getKind());
        Assertions.assertEquals("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984", eventImpl.getPubKey().toString());
        Assertions.assertEquals(1687765220, eventImpl.getCreatedAt());
        Assertions.assertEquals("手順書が間違ってたら作業者は無理だな", eventImpl.getContent());
        Assertions.assertEquals("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546",
                eventImpl.getSignature().toString());

        List<BaseTag> expectedTags = new ArrayList<>();
        EventTag eventTag = new EventTag("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346");
        eventTag.setRecommendedRelayUrl("");
        eventTag.setMarker(Marker.ROOT);
        expectedTags.add(eventTag);
        PubKeyTag pubKeyTag = new PubKeyTag();
        pubKeyTag.setPublicKey(new PublicKey("2bed79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984"));
        expectedTags.add(pubKeyTag);

        List<? extends BaseTag> actualTags = eventImpl.getTags();

        for (int i = 0; i < expectedTags.size(); i++) {
            BaseTag expected = expectedTags.get(i);
            if (expected instanceof EventTag expetedEventTag) {
                EventTag actualEventTag = (EventTag) actualTags.get(i);
                Assertions.assertEquals(expetedEventTag.getIdEvent(), actualEventTag.getIdEvent());
                Assertions.assertEquals(expetedEventTag.getRecommendedRelayUrl(), actualEventTag.getRecommendedRelayUrl());
                Assertions.assertEquals(expetedEventTag.getMarker(), actualEventTag.getMarker());
            } else if (expected instanceof PubKeyTag expectedPublicKeyTag) {
                PubKeyTag actualPublicKeyTag = (PubKeyTag) actualTags.get(i);
                Assertions.assertEquals(expectedPublicKeyTag.getPublicKey().toString(), actualPublicKeyTag.getPublicKey().toString());
            } else {
                Assertions.fail();
            }

        }

    }

}
