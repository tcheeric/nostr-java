package nostr.id;

import lombok.extern.slf4j.Slf4j;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.message.GenericMessage;
import nostr.event.tag.GenericTag;
import nostr.util.NostrUtil;
import nostr.util.validator.Nip05Validator;
import nostr.util.NostrException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static nostr.base.Encoder.ENCODER_MAPPED_AFTERBURNER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author squirrel
 */
@Slf4j
public class EventTest {

    public EventTest() {
    }

    @Test
    public void testCreateTextNoteEvent(){
        log.info("testCreateTextNoteEvent");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericEvent instance = EntityFactory.Events.createTextNoteEvent(publicKey);
        instance.update();
        assertNotNull(instance.getId());
        assertNotNull(instance.getCreatedAt());
        assertNull(instance.getSignature());
        final String bech32 = instance.toBech32();
        assertNotNull(bech32);
        assertDoesNotThrow(() -> {
            assertEquals(Bech32Prefix.NOTE.getCode(), Bech32.decode(bech32).hrp);
        });
    }

    @Test
    public void testCreateGenericTag() {
        log.info("testCreateGenericTag");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericTag genericTag = EntityFactory.Events.createGenericTag(publicKey);

        var encoder = new BaseTagEncoder(genericTag);
        var strJsonEvent = encoder.encode();

        assertDoesNotThrow(() -> {
            BaseTag tag = ENCODER_MAPPED_AFTERBURNER.readValue(strJsonEvent, BaseTag.class);
            assertEquals(genericTag, tag);
        });
    }

    @Test
    public void testCreateUnsupportedGenericTagAttribute() {
        /**
         * test of this functionality relocated to nostr-java-api {@link nostr.api.integration.ApiEventIT#testCreateUnsupportedGenericTagAttribute()}
         */
    }

    @Test
    @Disabled("Requires network access for nip05 validation")
    public void testNip05Validator() throws Exception {
        System.out.println("testNip05Validator");
        var nip05 = "nostr-java@nostr.band";
        var publicKey = new PublicKey(NostrUtil.hexToBytes(Bech32.fromBech32("npub126klq89p42wk78p4j5ur8wlxmxdqepdh8tez9e4axpd4run5nahsmff27j")));

        var nip05Validator = Nip05Validator.builder().nip05(nip05).publicKey(publicKey.toString()).build();

        assertThrows(NostrException.class, nip05Validator::validate);
    }

    @Test
    public void testAuthMessage() {
        System.out.println("testAuthMessage");

        GenericMessage msg = new GenericMessage("AUTH");
        String attr = "challenge-string";
        msg.addAttribute(ElementAttribute.builder().name("challenge").value(attr).build());

        var muattr = (msg.getAttributes().getFirst().getValue()).toString();
        assertEquals(attr, muattr);
    }

    @Test
    public void testEventIdConstraints() {
        log.info("testCreateTextNoteEvent");
        PublicKey publicKey = Identity.generateRandomIdentity().getPublicKey();
        GenericEvent genericEvent = EntityFactory.Events.createTextNoteEvent(publicKey);
        String id64chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
        assertDoesNotThrow(() -> genericEvent.setId(id64chars));

        String id63chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a71";
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> genericEvent.setId(id63chars))
                .getMessage().contains("[fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a71], length: [63], target length: [64]"));

        String id65chars = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a7123";
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> genericEvent.setId(id65chars))
                .getMessage().contains("[fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a7123], length: [65], target length: [64]"));
    }
}
