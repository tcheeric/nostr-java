package nostr.test.event;

import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;
import nostr.event.impl.ZapReceiptEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class ZapReceiptEventTest {
  @Test
  void testConstructZapReceiptEvent() {
    System.out.println("testConstructZapReceiptEvent");

    PublicKey sender = Identity.generateRandomIdentity().getPublicKey();
    String zapRequestPubKeyTag = Identity.generateRandomIdentity().getPublicKey().toString();
    String zapRequestEventTag = Identity.generateRandomIdentity().getPublicKey().toString();
    String zapRequestAddressTag = Identity.generateRandomIdentity().getPublicKey().toString();
    final String ZAP_RECEIPT_IDENTIFIER = "ipsum";
    final String ZAP_RECEIPT_RELAY_URI = "ws://localhost:5555";
    final String BOLT_11 = "bolt11";
    final String DESCRIPTION_SHA256 = "descriptionSha256";
    final String PRE_IMAGE = "preimage";

    ZapReceiptEvent instance = new ZapReceiptEvent(sender, zapRequestPubKeyTag, zapRequestEventTag, zapRequestAddressTag, ZAP_RECEIPT_IDENTIFIER, ZAP_RECEIPT_RELAY_URI, BOLT_11, DESCRIPTION_SHA256, PRE_IMAGE);

    Assertions.assertNotNull(instance.getZapReceipt());
    Assertions.assertNotNull(instance.getZapReceipt().getBolt11());
    Assertions.assertNotNull(instance.getZapReceipt().getDescriptionSha256());
    Assertions.assertNotNull(instance.getZapReceipt().getPreimage());

    Assertions.assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getPublicKey().toString()).anyMatch(zapRequestAddressTag::equals));

    Assertions.assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getRelay().getUri()).anyMatch(ZAP_RECEIPT_RELAY_URI::equals));

    Assertions.assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getIdentifierTag().getId()).anyMatch(ZAP_RECEIPT_IDENTIFIER::equals));

    Assertions.assertEquals(BOLT_11, instance.getZapReceipt().getBolt11());
    Assertions.assertEquals(DESCRIPTION_SHA256, instance.getZapReceipt().getDescriptionSha256());
    Assertions.assertEquals(PRE_IMAGE, instance.getZapReceipt().getPreimage());
  }
}