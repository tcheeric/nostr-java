package nostr.test.event;

import nostr.event.BaseTag;
import nostr.event.impl.ZapRequestEvent;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ZapRequestEventTest {
  @Test
  void testConstructZapRequestEvent() {
    System.out.println("testConstructZapRequestEvent");
    String sender = Identity.generateRandomIdentity().getPublicKey().toString();
    String recipient = Identity.generateRandomIdentity().getPublicKey().toString();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    final String ZAP_REQUEST_CONTENT = "zap request content";
    final Long AMOUNT = 1232456L;
    final String LNURL = "lnUrl";
    final String RELAYS_TAG = "relaystag";
    ZapRequestEvent instance = new ZapRequestEvent(sender, recipient, baseTags, ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG);

    Assertions.assertNotNull(instance.getTags());
    Assertions.assertNotNull(instance.getContent());
    Assertions.assertNotNull(instance.getZapRequest());

    Assertions.assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
    Assertions.assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
    Assertions.assertEquals(LNURL, instance.getZapRequest().getLnUrl());
    Assertions.assertEquals(AMOUNT, instance.getZapRequest().getAmount());
  }
}