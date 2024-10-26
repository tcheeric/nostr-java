package nostr.test.event;

import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP57Impl.ZapRequestEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log
public class NIP57ImplTest {

    @Test
    void testNIP57CreateZapRequestEventFactory() {
        log.info("testNIP57CreateZapRequestEventFactories");

        Identity sender = Identity.generateRandomIdentity();
        List<BaseTag> baseTags = new ArrayList<BaseTag>();
        PublicKey recipient = Identity.generateRandomIdentity().getPublicKey();
        final String ZAP_REQUEST_CONTENT = "zap request content";
        final Long AMOUNT = 1232456L;
        final String LNURL = "lnUrl";
        final String RELAYS_TAG = "ws://localhost:5555";

        ZapRequestEventFactory instance = new ZapRequestEventFactory(sender, recipient, baseTags, ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG);

        assertNotNull(instance.getIdentity());
        assertNotNull(instance.getTags());
        assertNotNull(instance.getContent());
        assertNotNull(instance.getZapRequest());
        assertNotNull(instance.getRecipientKey());

        assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
        assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
        assertEquals(LNURL, instance.getZapRequest().getLnUrl());
        assertEquals(AMOUNT, instance.getZapRequest().getAmount());
    }

}