package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP57;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ZapRequestEvent;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.Test;

@Slf4j
public class NIP57ImplTest {

  @Test
  void testNIP57CreateZapRequestEventFactory() throws NostrException {

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<>();
    PublicKey recipient = Identity.generateRandomIdentity().getPublicKey();
    final String ZAP_REQUEST_CONTENT = "zap request content";
    final Long AMOUNT = 1232456L;
    final String LNURL = "lnUrl";
    final String RELAYS_URL = "ws://localhost:5555";

    // ZapRequestEventFactory genericEvent = new ZapRequestEventFactory(sender, recipient, baseTags,
    // ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG);
    NIP57 nip57 = new NIP57(sender);
    GenericEvent genericEvent =
        nip57
            .createZapRequestEvent(
                AMOUNT,
                LNURL,
                BaseTag.create("relays", RELAYS_URL),
                ZAP_REQUEST_CONTENT,
                recipient,
                null,
                null)
            .getEvent();

    ZapRequestEvent zapRequestEvent = GenericEvent.convert(genericEvent, ZapRequestEvent.class);

    assertNotNull(zapRequestEvent.getId());
    assertNotNull(zapRequestEvent.getTags());
    assertNotNull(zapRequestEvent.getContent());
    assertNotNull(zapRequestEvent.getZapRequest());
    assertNotNull(zapRequestEvent.getRecipientKey());

    assertTrue(
        zapRequestEvent.getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_URL)));
    assertEquals(ZAP_REQUEST_CONTENT, genericEvent.getContent());
    assertEquals(LNURL, zapRequestEvent.getLnUrl());
    assertEquals(AMOUNT, zapRequestEvent.getAmount());
  }
}
