package nostr.test.api;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import nostr.api.NIP44;
import nostr.api.NIP60;
import nostr.base.Mint;
import nostr.base.Proof;
import nostr.base.Quote;
import nostr.base.Relay;
import nostr.base.Token;
import nostr.base.Wallet;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

public class NIP60Test {

    @Test
    public void createWalletEvent() throws JsonProcessingException {

        // Prepare
        Mint mint1 = new Mint("https://mint1");
        mint1.setUnits(Arrays.asList("sat"));

        Mint mint2 = new Mint("https://mint2");
        mint2.setUnits(Arrays.asList("sat"));

        Mint mint3 = new Mint("https://mint3");
        mint3.setUnits(Arrays.asList("sat"));

        Relay relay1 = new Relay("wss://relay1");
        Relay relay2 = new Relay("wss://relay2");

        Wallet wallet = new Wallet();
        wallet.setId("my-wallet");
        wallet.setName("my shitposting wallet");
        wallet.setDescription("a wallet for my day-to-day shitposting");
        wallet.setBalance(100);
        wallet.setPrivateKey("hexkey");
        wallet.setUnit("sat");
        wallet.setMints(Set.of(mint1, mint2, mint3));
        wallet.setRelays(Set.of(relay1, relay2));

        Identity sender = Identity.generateRandomIdentity();
        NIP60<GenericEvent> nip60 = new NIP60<>(sender);

        // Create
        GenericEvent event = nip60.createWalletEvent(wallet).getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert kind
        Assertions.assertEquals(37375, event.getKind().intValue());

        // Assert tags
        Assertions.assertEquals(10, tags.size());

        // Assert relay tags
        List<BaseTag> relayTags = tags.stream()
                .filter(tag -> tag.getCode().equals("relay"))
                .toList();

        Assertions.assertEquals(2, relayTags.size());

        // Assert mint tags
        List<BaseTag> mintTags = tags.stream()
                .filter(tag -> tag.getCode().equals("mint"))
                .toList();

        Assertions.assertEquals(3, mintTags.size());

        // Decrypt and verify content
        String decryptedContent = NIP44.decrypt(sender, event.getContent(), sender.getPublicKey());
        ObjectMapper mapper = new ObjectMapper();
        GenericTag[] contentTags = mapper.readValue(decryptedContent, GenericTag[].class);

        // First tag should be balance
        Assertions.assertEquals("balance", contentTags[0].getCode());
        Assertions.assertEquals("100", contentTags[0].getAttributes().get(0).getValue());
        Assertions.assertEquals("sat", contentTags[0].getAttributes().get(1).getValue());

        // Second tag should be privkey
        Assertions.assertEquals("privkey", contentTags[1].getCode());
        Assertions.assertEquals("hexkey", contentTags[1].getAttributes().get(0).getValue());
    }

    @Test
    public void createTokenEvent() throws JsonProcessingException {

        // Prepare
        Mint mint = new Mint("https://stablenut.umint.cash");
        mint.setUnits(Arrays.asList("sat"));

        Wallet wallet = new Wallet();
        wallet.setId("my-wallet");
        wallet.setName("my shitposting wallet");
        wallet.setDescription("a wallet for my day-to-day shitposting");
        wallet.setBalance(100);
        wallet.setPrivateKey("hexkey");
        wallet.setUnit("sat");
        wallet.setMints(Set.of(mint));

        Proof proof = new Proof();
        proof.setId("005c2502034d4f12");
        proof.setAmount(1);
        proof.setSecret("z+zyxAVLRqN9lEjxuNPSyRJzEstbl69Jc1vtimvtkPg=");
        proof.setC("0241d98a8197ef238a192d47edf191a9de78b657308937b4f7dd0aa53beae72c46");

        Token token = new Token();
        token.setMint(mint);
        token.setProofs(Arrays.asList(proof));

        Identity sender = Identity.generateRandomIdentity();
        NIP60<GenericEvent> nip60 = new NIP60<>(sender);

        // Create
        GenericEvent event = nip60.createTokenEvent(token, wallet).getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert kind
        Assertions.assertEquals(7375, event.getKind().intValue());

        // Assert tags
        Assertions.assertEquals(1, tags.size());

        // Assert a-tag
        AddressTag aTag = (AddressTag) tags.get(0);
        Assertions.assertEquals("a", aTag.getCode());
        // Assertions.assertEquals("<pubkey>", aTag.getPublicKey());
        Assertions.assertEquals("my-wallet", aTag.getIdentifierTag().getId());
        Assertions.assertEquals(37375, aTag.getKind().intValue());

        // Decrypt and verify content
        String decryptedContent = NIP44.decrypt(sender, event.getContent(), sender.getPublicKey());
        ObjectMapper mapper = new ObjectMapper();
        Token contentToken = mapper.readValue(decryptedContent, Token.class);
        Assertions.assertEquals("https://stablenut.umint.cash", contentToken.getMint().getUrl());

        Proof proofContent = contentToken.getProofs().get(0);
        Assertions.assertEquals(proof.getId(), proofContent.getId());
        Assertions.assertEquals(proof.getAmount(), proofContent.getAmount());
        Assertions.assertEquals(proof.getSecret(), proofContent.getSecret());
        Assertions.assertEquals(proof.getC(), proofContent.getC());
    }

    @Test
    public void createSpendingHistoryEvent() throws JsonProcessingException {

        NIP60.SpendingHistory.Amount amount = new NIP60.SpendingHistory.Amount();
        amount.setAmount(1);
        amount.setUnit("sat");

        EventTag eventTag = new EventTag();
        eventTag.setIdEvent("<event-id-of-spent-token>");
        eventTag.setRecommendedRelayUrl("<relay-hint>");
        eventTag.setMarker(Marker.CREATED);

        NIP60.SpendingHistory spendingHistory = new NIP60.SpendingHistory();
        spendingHistory.setDirection(NIP60.SpendingHistory.Direction.RECEIVED);
        spendingHistory.setAmount(amount);
        spendingHistory.setEventTags(Arrays.asList(eventTag));

        Identity sender = Identity.generateRandomIdentity();
        NIP60<GenericEvent> nip60 = new NIP60<>(sender);

        Wallet wallet = new Wallet();
        wallet.setId("my-wallet");
        wallet.setName("my shitposting wallet");
        wallet.setDescription("a wallet for my day-to-day shitposting");
        wallet.setBalance(100);
        wallet.setPrivateKey("hexkey");
        wallet.setUnit("sat");

        GenericEvent event = nip60.createSpendingHistoryEvent(spendingHistory, wallet).getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert tags
        Assertions.assertEquals(1, tags.size());
        Assertions.assertEquals(7376, event.getKind().intValue());

        // Assert a-tag
        AddressTag aTag = (AddressTag) tags.get(0);
        Assertions.assertEquals("my-wallet", aTag.getIdentifierTag().getId());
        Assertions.assertEquals(37375, aTag.getKind().intValue());

        // Decrypt and verify content
        String decryptedContent = NIP44.decrypt(sender, event.getContent(), sender.getPublicKey());
        ObjectMapper mapper = new ObjectMapper();
        BaseTag[] contentTags = mapper.readValue(decryptedContent, BaseTag[].class);

        // Assert direction
        GenericTag directionTag = (GenericTag) contentTags[0];
        Assertions.assertEquals("direction", directionTag.getCode());
        Assertions.assertEquals("in",
                directionTag.getAttributes().get(0).getValue().toString());

        // Assert amount
        GenericTag amountTag = (GenericTag) contentTags[1];
        Assertions.assertEquals("amount", amountTag.getCode());
        Assertions.assertEquals("1", amountTag.getAttributes().get(0).getValue());
        Assertions.assertEquals("sat", amountTag.getAttributes().get(1).getValue());

        // Assert event
        EventTag eTag = (EventTag) contentTags[2];
        Assertions.assertEquals("e", eTag.getCode());
        Assertions.assertEquals("<event-id-of-spent-token>", eTag.getIdEvent());
        Assertions.assertEquals("<relay-hint>", eTag.getRecommendedRelayUrl());
        Assertions.assertEquals("created", eTag.getMarker().getValue());
    }

    @Test
    public void createRedemptionQuoteEvent() {

        Wallet wallet = new Wallet();
        wallet.setId("my-wallet");
        wallet.setName("my shitposting wallet");
        wallet.setDescription("a wallet for my day-to-day shitposting");
        wallet.setBalance(100);
        wallet.setPrivateKey("hexkey");
        wallet.setUnit("sat");

        Quote quote = new Quote();
        quote.setId("quote-id");
        quote.setExpiration(1728883200L);
        quote.setMint(new Mint("<mint-url>"));
        quote.setWallet(wallet);

        Identity sender = Identity.generateRandomIdentity();
        NIP60<GenericEvent> nip60 = new NIP60<>(sender);

        GenericEvent event = nip60.createRedemptionQuoteEvent(quote).getEvent();
        List<BaseTag> tags = event.getTags();

        // Assert kind
        Assertions.assertEquals(7374, event.getKind().intValue());

        // Assert tags
        Assertions.assertEquals(3, tags.size());

        // Assert Expiration tag
        GenericTag expirationTag = (GenericTag) tags.get(0);
        Assertions.assertEquals("expiration", expirationTag.getCode());
        Assertions.assertEquals("1728883200", expirationTag.getAttributes().get(0).getValue());

        // Assert Mint tag
        GenericTag mintTag = (GenericTag) tags.get(1);
        Assertions.assertEquals("mint", mintTag.getCode());
        Assertions.assertEquals("<mint-url>", mintTag.getAttributes().get(0).getValue());

        // Assert a-tag
        AddressTag aTag = (AddressTag) tags.get(2);
        Assertions.assertEquals("my-wallet", aTag.getIdentifierTag().getId());
        Assertions.assertEquals(37375, aTag.getKind().intValue());

    }

    private String getMintUrl(@NonNull BaseTag tag) {
        if (tag instanceof GenericTag mintTag) {
            return mintTag.getAttributes().get(0).getValue().toString();
        }
        return null;
    }

    private String getRelayUrl(@NonNull BaseTag tag) {
        if (tag instanceof GenericTag relayTag) {
            return relayTag.getAttributes().get(0).getValue().toString();
        }
        return null;
    }
}
