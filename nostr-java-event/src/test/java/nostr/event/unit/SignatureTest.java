package nostr.event.unit;

import nostr.base.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SignatureTest {
    @Test
    public void testSignatureStringLength() {
        assertDoesNotThrow(() ->
                Signature.fromString("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546"));

        assertTrue(
                assertThrows(AssertionError.class, () -> Signature.fromString("86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546a"))
                        .getMessage().contains("[129], target length: [128]"));
    }
}
