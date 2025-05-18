package nostr.id;

import lombok.extern.java.Log;

@Log
class ZapReceiptEventTest {

/*
    @Test
    void testConstructZapReceiptEvent() {

        log.info("testConstructZapReceiptEvent");

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

        assertNotNull(instance.getZapReceipt());
        assertNotNull(instance.getZapReceipt().getBolt11());
        assertNotNull(instance.getZapReceipt().getDescriptionSha256());
        assertNotNull(instance.getZapReceipt().getPreimage());

        assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getPublicKey().toString()).anyMatch(zapRequestAddressTag::equals));

        assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getRelay().getUri()).anyMatch(ZAP_RECEIPT_RELAY_URI::equals));

        assertTrue(instance.getTags().stream().filter(AddressTag.class::isInstance).map(AddressTag.class::cast).map(addressTag -> addressTag.getIdentifierTag().getId()).anyMatch(ZAP_RECEIPT_IDENTIFIER::equals));

        assertEquals(BOLT_11, instance.getZapReceipt().getBolt11());
        assertEquals(DESCRIPTION_SHA256, instance.getZapReceipt().getDescriptionSha256());
        assertEquals(PRE_IMAGE, instance.getZapReceipt().getPreimage());
    }
*/

}
