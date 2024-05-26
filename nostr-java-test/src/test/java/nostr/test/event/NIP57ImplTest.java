package nostr.test.event;

import nostr.api.factory.impl.NIP57Impl.ZapRequestEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NIP57ImplTest {
  @Test
  void testNIP57CreateZapRequestEventFactory() {
    System.out.println("testNIP57CreateZapRequestEventFactories");

    Identity sender = Identity.generateRandomIdentity();
    List<BaseTag> baseTags = new ArrayList<BaseTag>();
    PublicKey recipient = Identity.generateRandomIdentity().getPublicKey();
    final String ZAP_REQUEST_CONTENT = "zap request content";
    final Long AMOUNT = 1232456L;
    final String LNURL = "lnUrl";
    final String RELAYS_TAG = "relaystag";

    ZapRequestEventFactory instance = new ZapRequestEventFactory(sender, recipient, baseTags, ZAP_REQUEST_CONTENT, AMOUNT, LNURL, RELAYS_TAG);

    Assertions.assertNotNull(instance.getIdentity());
    Assertions.assertNotNull(instance.getTags());
    Assertions.assertNotNull(instance.getContent());
    Assertions.assertNotNull(instance.getZapRequest());
    Assertions.assertNotNull(instance.getRecipientKey());

    Assertions.assertTrue(instance.getZapRequest().getRelaysTag().getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_TAG)));
    Assertions.assertEquals(ZAP_REQUEST_CONTENT, instance.getContent());
    Assertions.assertEquals(LNURL, instance.getZapRequest().getLnUrl());
    Assertions.assertEquals(AMOUNT, instance.getZapRequest().getAmount());
  }
}