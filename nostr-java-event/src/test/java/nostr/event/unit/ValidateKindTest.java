package nostr.event.unit;

import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.impl.TextNoteEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidateKindTest {
    @Test
    public void testTextNoteInvalidKind() {
        TextNoteEvent event = new TextNoteEvent();
        event.setPubKey(new PublicKey("bbbd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984"));
        event.setKind(Kind.DELETION.getValue());
        event.setContent("");
        event.setTags(new ArrayList<>());
        event.setSignature(Signature.fromString("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"));
        event.setCreatedAt(0L);
        event.setId("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4346");

        assertThrows(AssertionError.class, event::validate);
    }
}
