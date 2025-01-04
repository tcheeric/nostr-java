package nostr.test.api;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nostr.api.NIP60;
import nostr.api.NIP61;
import nostr.base.Mint;
import nostr.base.Proof;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

public class NIP61Test {

    @Test
    public void createNutzapInformationalEvent() {
        // Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP61<GenericEvent> nip61 = new NIP61<>(sender);

        // Create test data
        List<String> pubkeys = Arrays.asList("pubkey1", "pubkey2");
        List<Relay> relays = Arrays.asList(
                new Relay("wss://relay1.example.com"),
                new Relay("wss://relay2.example.com"));
        List<Mint> mints = Arrays.asList(
                new Mint("https://mint1.example.com"),
                new Mint("https://mint2.example.com"));

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

    @Test
    public void createNutzapEvent() {
        // Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP61<GenericEvent> nip61 = new NIP61<>(sender);

        Identity recipientId = Identity.generateRandomIdentity();

        // Create test data
        NIP60.SpendingHistory.Amount amount = new NIP60.SpendingHistory.Amount(100, "sat");
        Mint mint = new Mint("https://mint.example.com");
        // PublicKey recipient = new PublicKey("recipient-pubkey");
        String content = "Test content";

        // Optional proofs and events
        Proof proof = new Proof();
        proof.setId("test-proof-id");
        List<Proof> proofs = Arrays.asList(proof);

        EventTag eventTag = new EventTag();
        eventTag.setIdEvent("test-event-id");
        List<EventTag> events = Arrays.asList(eventTag);

        // Create event
        GenericEvent event = nip61.createNutzapEvent(amount, proofs, mint, events, recipientId.getPublicKey(), content)
                .getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert tags
        Assertions.assertEquals(4, tags.size()); // url + amount + unit + pubkey

        // Verify url tag
        List<GenericTag> urlTags = tags.stream()
                .filter(tag -> tag.getCode().equals("u"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(1, urlTags.size());
        Assertions.assertEquals("https://mint.example.com", urlTags.get(0).getAttributes().get(0).getValue());

        // Verify amount tag
        List<GenericTag> amountTags = tags.stream()
                .filter(tag -> tag.getCode().equals("amount"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(1, amountTags.size());
        Assertions.assertEquals("100", amountTags.get(0).getAttributes().get(0).getValue());

        // Verify unit tag
        List<GenericTag> unitTags = tags.stream()
                .filter(tag -> tag.getCode().equals("unit"))
                .map(tag -> (GenericTag) tag)
                .toList();
        Assertions.assertEquals(1, unitTags.size());
        Assertions.assertEquals("sat", unitTags.get(0).getAttributes().get(0).getValue());

        // Verify pubkey tag
        List<PubKeyTag> pubkeyTags = tags.stream()
                .filter(tag -> tag.getCode().equals("p"))
                .map(tag -> (PubKeyTag) tag)
                .toList();
        Assertions.assertEquals(1, pubkeyTags.size());
        Assertions.assertEquals(recipientId.getPublicKey().toString(), pubkeyTags.get(0).getPublicKey().toString());

        // Assert content
        Assertions.assertEquals(content, event.getContent());
    }

    @Test
    public void createTags() {
        // Test P2PK tag creation
        String pubkey = "test-pubkey";
        GenericTag p2pkTag = (GenericTag) NIP61.createP2pkTag(pubkey);
        Assertions.assertEquals("pubkey", p2pkTag.getCode());
        Assertions.assertEquals(pubkey, p2pkTag.getAttributes().get(0).getValue());

        // Test URL tag creation
        String url = "https://example.com";
        GenericTag urlTag = (GenericTag) NIP61.createUrlTag(url);
        Assertions.assertEquals("u", urlTag.getCode());
        Assertions.assertEquals(url, urlTag.getAttributes().get(0).getValue());

        // Test Proof tag creation
        Proof proof = new Proof();
        proof.setId("test-proof-id");
        GenericTag proofTag = (GenericTag) NIP61.createProofTag(proof);
        Assertions.assertEquals("proof", proofTag.getCode());
        Assertions.assertTrue(proofTag.getAttributes().get(0).getValue().toString().contains("test-proof-id"));
    }
}
