package nostr.api.unit;

import lombok.extern.java.Log;
import nostr.api.NIP57;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ZapRequestEvent;
import nostr.event.tag.GenericTag;
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
        List<BaseTag> baseTags = new ArrayList<>();
        PublicKey recipient = Identity.generateRandomIdentity().getPublicKey();
        final String ZAP_REQUEST_CONTENT = "zap request content";
        final Long AMOUNT = 1232456L;
        final String LNURL = "lnUrl";
        final String RELAYS_TAG = "ws://localhost:5555";

        //ZapRequestEventFactory genericEvent = new ZapRequestEventFactory(sender, recipient, baseTags, ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG);
        NIP57 nip57 = new NIP57(sender);
        GenericEvent genericEvent = nip57.createZapRequestEvent(
                AMOUNT,
                LNURL,
                new GenericTag("relays", new ElementAttribute("relays", RELAYS_TAG)),
                ZAP_REQUEST_CONTENT,
                recipient,
                null,
                null).getEvent();

        ZapRequestEvent zapRequestEvent = GenericEvent.convert(genericEvent, ZapRequestEvent.class);

        assertNotNull(zapRequestEvent.getId());
        assertNotNull(zapRequestEvent.getTags());
        assertNotNull(zapRequestEvent.getContent());
        assertNotNull(zapRequestEvent.getZapRequest());
        assertNotNull(zapRequestEvent.getRecipientKey());

        assertTrue(zapRequestEvent.getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
        assertEquals(ZAP_REQUEST_CONTENT, genericEvent.getContent());
        assertEquals(LNURL, zapRequestEvent.getLnUrl());
        assertEquals(AMOUNT, zapRequestEvent.getAmount());
    }

}
