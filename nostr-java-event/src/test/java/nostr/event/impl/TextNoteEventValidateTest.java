package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.tag.PubKeyTag;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TextNoteEventValidateTest {
    private static final String HEX_64_A = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String HEX_64_B = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final String SIG_HEX = "e".repeat(128);

    private TextNoteEvent createValidEvent() {
        PublicKey pubKey = new PublicKey(HEX_64_A);
        List<BaseTag> tags = new ArrayList<>();
        tags.add(new PubKeyTag(new PublicKey(HEX_64_B)));
        TextNoteEvent event = new TextNoteEvent(pubKey, tags, "note content");
        event.setId(HEX_64_A);
        event.setSignature(Signature.fromString(SIG_HEX));
        event.setCreatedAt(Instant.now().getEpochSecond());
        return event;
    }

    private void clearTags(TextNoteEvent event) {
        try {
            Field f = GenericEvent.class.getDeclaredField("tags");
            f.setAccessible(true);
            f.set(event, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testValidateSuccess() {
        TextNoteEvent event = createValidEvent();
        assertDoesNotThrow(event::validate);
    }

    @Test
    public void testValidateMissingTags() {
        TextNoteEvent event = createValidEvent();
        clearTags(event);
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateWrongKind() {
        TextNoteEvent event = createValidEvent();
        event.setKind(-1);
        assertThrows(AssertionError.class, event::validate);
    }

    @Test
    public void testValidateInvalidContent() {
        TextNoteEvent event = createValidEvent();
        event.setContent(null);
        assertThrows(AssertionError.class, event::validate);
    }
}
