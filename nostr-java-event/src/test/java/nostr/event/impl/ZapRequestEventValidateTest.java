package nostr.event.impl;

import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.GenericTag;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ZapRequestEventValidateTest {
    private static final String HEX_64_A = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String HEX_64_B = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final String SIG_HEX = "d".repeat(128);

    private ZapRequestEvent createValidEvent() {
        PublicKey pubKey = new PublicKey(HEX_64_A);
        List<BaseTag> tags = new ArrayList<>();
        tags.add(new PubKeyTag(new PublicKey(HEX_64_B)));
        GenericTag amountTag = new GenericTag("amount");
        amountTag.addAttribute(new ElementAttribute("amount", "1000"));
        tags.add(amountTag);
        GenericTag lnurlTag = new GenericTag("lnurl");
        lnurlTag.addAttribute(new ElementAttribute("lnurl", "lnurl-value"));
        tags.add(lnurlTag);
        ZapRequestEvent event = new ZapRequestEvent(pubKey, tags, "content");
        event.setId(HEX_64_A);
        event.setSignature(Signature.fromString(SIG_HEX));
        event.setCreatedAt(Instant.now().getEpochSecond());
        return event;
    }

    @Test
    public void testValidateSuccess() {
        ZapRequestEvent event = createValidEvent();
        assertDoesNotThrow(event::validate);
    }

    @Test
    public void testValidateMissingPTag() {
        ZapRequestEvent event = createValidEvent();
        event.setTags(event.getTags().subList(1, event.getTags().size()));
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateMissingAmountTag() {
        ZapRequestEvent event = createValidEvent();
        event.getTags().removeIf(t -> "amount".equals(t.getCode()));
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateMissingLnurlTag() {
        ZapRequestEvent event = createValidEvent();
        event.getTags().removeIf(t -> "lnurl".equals(t.getCode()));
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateWrongKind() {
        ZapRequestEvent event = createValidEvent();
        event.setKind(-1);
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateInvalidContent() {
        ZapRequestEvent event = createValidEvent();
        event.setContent(null);
        assertThrows(AssertionError.class, event::validate);
    }
}
