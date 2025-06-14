package nostr.api.unit;

import lombok.SneakyThrows;
import nostr.api.NIP61;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.entities.Amount;
import nostr.event.entities.CashuMint;
import nostr.event.entities.CashuProof;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.UrlTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class NIP61Test {

    @Test
    public void createNutzapInformationalEvent() {
        // Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP61 nip61 = new NIP61(sender);

        // Create test data
        List<String> pubkeys = Arrays.asList("pubkey1", "pubkey2");
        List<Relay> relays = Arrays.asList(
                new Relay("wss://relay1.example.com"),
                new Relay("wss://relay2.example.com"));
        List<CashuMint> mints = Arrays.asList(
                new CashuMint("https://mint1.example.com"),
                new CashuMint("https://mint2.example.com"));

        // Create event
        GenericEvent event = nip61.createNutzapInformationalEvent(pubkeys, relays, mints).getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert tags
        Assertions.assertEquals(6, tags.size()); // 2 pubkeys + 2 relays + 2 mints

        // Verify pubkey tags
        List<GenericTag> pubkeyTags = tags.stream()
                .filter(tag -> tag.getCode().equals("pubkey"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(2, pubkeyTags.size());
        Assertions.assertEquals("pubkey1", pubkeyTags.get(0).getAttributes().get(0).getValue());
        Assertions.assertEquals("pubkey2", pubkeyTags.get(1).getAttributes().get(0).getValue());

        // Verify relay tags
        List<GenericTag> relayTags = tags.stream()
                .filter(tag -> tag.getCode().equals("relay"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(2, relayTags.size());
        Assertions.assertEquals("wss://relay1.example.com", relayTags.get(0).getAttributes().get(0).getValue());
        Assertions.assertEquals("wss://relay2.example.com", relayTags.get(1).getAttributes().get(0).getValue());

        // Verify mint tags
        List<GenericTag> mintTags = tags.stream()
                .filter(tag -> tag.getCode().equals("mint"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(2, mintTags.size());
        Assertions.assertEquals("https://mint1.example.com", mintTags.get(0).getAttributes().get(0).getValue());
        Assertions.assertEquals("https://mint2.example.com", mintTags.get(1).getAttributes().get(0).getValue());
    }

    @SneakyThrows
    @Test
    public void createNutzapEvent() {
        // Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP61 nip61 = new NIP61(sender);

        Identity recipientId = Identity.generateRandomIdentity();

        // Create test data
        Amount amount = new Amount(100, "sat");
        CashuMint mint = new CashuMint("https://mint.example.com");
        // PublicKey recipient = new PublicKey("recipient-pubkey");
        String content = "Test content";

        // Optional proofs and events
        CashuProof proof = new CashuProof();
        proof.setId("test-proof-id");
        List<CashuProof> proofs = List.of(proof);

        EventTag eventTag = new EventTag();
        eventTag.setIdEvent("test-event-id");
        List<EventTag> events = List.of(eventTag);

        // Create event
        GenericEvent event = nip61.createNutzapEvent(
                        amount,
                        proofs,
                        URI.create(mint.getUrl()).toURL(),
                        events,
                        recipientId.getPublicKey(),
                        content)
                .getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert tags
        Assertions.assertEquals(6, tags.size()); // url + amount + unit + pubkey

        // Verify url tag
        List<BaseTag> urlTags = tags.stream()
                .filter(tag -> tag.getCode().equals("u"))
                .toList();
        assertInstanceOf(UrlTag.class, urlTags.get(0));
        Assertions.assertEquals(1, urlTags.size());
        Assertions.assertEquals("https://mint.example.com", ((UrlTag) urlTags.get(0)).getUrl());

        // Verify amount tag
        List<BaseTag> amountTags = tags.stream()
                .filter(tag -> tag.getCode().equals("amount"))
                .toList();
        assertInstanceOf(GenericTag.class, amountTags.get(0));
        Assertions.assertEquals(1, amountTags.size());
        Assertions.assertEquals("100", ((GenericTag) amountTags.get(0)).getAttributes().get(0).getValue());

        // Verify unit tag
        List<BaseTag> unitTags = tags.stream()
                .filter(tag -> tag.getCode().equals("unit"))
                .toList();
        assertInstanceOf(GenericTag.class, unitTags.get(0));
        Assertions.assertEquals(1, unitTags.size());
        Assertions.assertEquals("sat", ((GenericTag) unitTags.get(0)).getAttributes().get(0).getValue());

        // Verify pubkey tag
        List<BaseTag> pubkeyTags = tags.stream()
                .filter(tag -> tag.getCode().equals("p"))
                .toList();
        assertInstanceOf(PubKeyTag.class, pubkeyTags.get(0));
        Assertions.assertEquals(1, pubkeyTags.size());
        Assertions.assertEquals(recipientId.getPublicKey().toString(), ((PubKeyTag)pubkeyTags.get(0)).getPublicKey().toString());

        // Assert content
        Assertions.assertEquals(content, event.getContent());
    }

    @Test
    public void createTags() {
        // Test P2PK tag creation
        String pubkey = "test-pubkey";
        BaseTag p2pkTag = NIP61.createP2pkTag(pubkey);
        assertInstanceOf(GenericTag.class, p2pkTag);
        Assertions.assertEquals("pubkey", p2pkTag.getCode());
        Assertions.assertEquals(pubkey, ((GenericTag) p2pkTag).getAttributes().get(0).getValue());

        // Test URL tag creation
        String url = "https://example.com";
        BaseTag urlTag = NIP61.createUrlTag(url);
        assertInstanceOf(UrlTag.class, urlTag);
        Assertions.assertEquals("u", urlTag.getCode());
        Assertions.assertEquals(url, ((UrlTag) urlTag).getUrl());

        // Test CashuProof tag creation
        CashuProof proof = new CashuProof();
        proof.setId("test-proof-id");
        BaseTag proofTag = NIP61.createProofTag(proof);
        assertInstanceOf(GenericTag.class, proofTag);
        Assertions.assertEquals("proof", proofTag.getCode());
        Assertions.assertTrue(((GenericTag) proofTag).getAttributes().get(0).getValue().toString().contains("test-proof-id"));
    }
}
