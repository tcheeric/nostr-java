package nostr.event.unit;

import nostr.base.PublicKey;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EmojiTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.ExpirationTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.NonceTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;
import nostr.event.tag.RelaysTag;
import nostr.event.tag.SubjectTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GenericTagTest {

    @Test
    public void testCreateAddressTag() {
        String publicKey = "bbbd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";

        String code = "a";
        List<String> params = List.of("30023:" + publicKey + ":test");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(AddressTag.class, tag);
        assertEquals(code, tag.getCode());

        AddressTag addressTag = (AddressTag) tag;
        assertEquals("30023", addressTag.getKind().toString());
        assertEquals(publicKey, addressTag.getPublicKey().toString());
        assertEquals("test", addressTag.getIdentifierTag().getId());
    }

    @Test
    public void testCreateEventTag() {
        String code = "e";
        List<String> params = List.of("123abc", "wss://relay.example.com", "ROOT");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(EventTag.class, tag);
        assertEquals(code, tag.getCode());

        EventTag eventTag = (EventTag) tag;
        assertEquals("123abc", eventTag.getIdEvent());
        assertEquals("wss://relay.example.com", eventTag.getRecommendedRelayUrl());
        assertEquals("root", eventTag.getMarker().getValue());
    }

    @Test
    public void testCreatePubKeyTag() {
        String publicKey = "bbbd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";

        String code = "p";
        List<String> params = List.of(publicKey, "wss://relay.example.com");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(PubKeyTag.class, tag);
        assertEquals(code, tag.getCode());

        PubKeyTag pubKeyTag = (PubKeyTag) tag;
        assertEquals(publicKey, pubKeyTag.getPublicKey().toString());
        assertEquals("wss://relay.example.com", pubKeyTag.getMainRelayUrl());
    }

    @Test
    public void testCreateEmojiTag() {
        String code = "emoji";
        List<String> params = List.of("😊", "http://smile.com/icon.gif");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(EmojiTag.class, tag);
        assertEquals(code, tag.getCode());

        EmojiTag emojiTag = (EmojiTag) tag;
        assertEquals("http://smile.com/icon.gif", emojiTag.getUrl());
        assertEquals("😊", emojiTag.getShortcode());
    }

    @Test
    public void testCreatePriceTag() {
        String code = "price";
        List<String> params = List.of("10.99", "USD");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(PriceTag.class, tag);
        assertEquals(code, tag.getCode());

        PriceTag priceTag = (PriceTag) tag;
        assertEquals("10.99", priceTag.getNumber().toString());
        assertEquals("USD", priceTag.getCurrency());
    }

    @Test
    public void testCreateExpirationTag() {
        String code = "expiration";
        int timestamp = 1735689600;
        List<String> params = List.of(String.valueOf(timestamp));
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(ExpirationTag.class, tag);
        assertEquals(code, tag.getCode());

        ExpirationTag expirationTag = (ExpirationTag) tag;
        assertEquals(timestamp, expirationTag.getExpiration());
    }

    @Test
    public void testCreateRelaysTag() {
        String code = "relays";
        List<String> params = List.of("wss://relay1.com", "wss://relay2.com");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(RelaysTag.class, tag);
        assertEquals(code, tag.getCode());

        RelaysTag relaysTag = (RelaysTag) tag;
        assertEquals(2, relaysTag.getRelays().size());
        assertEquals("wss://relay1.com", relaysTag.getRelays().get(0).getUri());
        assertEquals("wss://relay2.com", relaysTag.getRelays().get(1).getUri());
    }

    @Test
    public void testCreateIdentifierTag() {
        String code = "d";
        List<String> params = List.of("test-identifier");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(IdentifierTag.class, tag);
        assertEquals(code, tag.getCode());

        IdentifierTag identifierTag = (IdentifierTag) tag;
        assertEquals("test-identifier", identifierTag.getId());
    }

    @Test
    public void testCreateGeohashTag() {
        String code = "g";
        List<String> params = List.of("u4pruydqqvj");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(GeohashTag.class, tag);
        assertEquals(code, tag.getCode());

        GeohashTag geohashTag = (GeohashTag) tag;
        assertEquals("u4pruydqqvj", geohashTag.getLocation());
    }

    @Test
    public void testCreateLabelTag() {
        String code = "l";
        List<String> params = List.of("test-label", "namespace");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(LabelTag.class, tag);
        assertEquals(code, tag.getCode());

        LabelTag labelTag = (LabelTag) tag;
        assertEquals("test-label", labelTag.getLabel());
        assertEquals("namespace", labelTag.getNameSpace());
    }

    @Test
    public void testCreateLabelNameSpaceTag() {
        String code = "L";
        List<String> params = List.of("namespace");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(LabelNamespaceTag.class, tag);
        assertEquals(code, tag.getCode());

        LabelNamespaceTag labelNamespaceTag = (LabelNamespaceTag) tag;
        assertEquals("namespace", labelNamespaceTag.getNameSpace());
    }

    @Test
    public void testCreateReferenceTag() {
        String code = "r";
        List<String> params = List.of("wss://relay.example.com");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(ReferenceTag.class, tag);
        assertEquals(code, tag.getCode());

        ReferenceTag referenceTag = (ReferenceTag) tag;
        assertEquals("wss://relay.example.com", referenceTag.getUri().toString());
    }

    @Test
    public void testCreateHashtagTag() {
        String code = "t";
        List<String> params = List.of("nostr");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(HashtagTag.class, tag);
        assertEquals(code, tag.getCode());

        HashtagTag hashtagTag = (HashtagTag) tag;
        assertEquals("nostr", hashtagTag.getHashTag());
    }

    @Test
    public void testCreateNonceTag() {
        String code = "nonce";
        List<String> params = List.of("123456", "20");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(NonceTag.class, tag);
        assertEquals(code, tag.getCode());

        NonceTag nonceTag = (NonceTag) tag;
        assertEquals("123456", nonceTag.getNonce().toString());
        assertEquals(20, nonceTag.getDifficulty());
    }

    @Test
    public void testCreateSubjectTag() {
        String code = "subject";
        List<String> params = List.of("Test Subject");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(SubjectTag.class, tag);
        assertEquals(code, tag.getCode());

        SubjectTag subjectTag = (SubjectTag) tag;
        assertEquals("Test Subject", subjectTag.getSubject());
    }

    @Test
    public void testCreateGenericFallback() {
        String code = "unknown";
        List<String> params = List.of("test-value");
        GenericTag tag = GenericTag.create(code, params);

        assertInstanceOf(GenericTag.class, tag);
        assertEquals(code, tag.getCode());
        assertEquals("test-value", tag.getAttributes().get(0).getValue());
    }
}