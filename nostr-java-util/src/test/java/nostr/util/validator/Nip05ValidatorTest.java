package nostr.util.validator;

import nostr.util.NostrException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Nip05ValidatorTest {

    @Test
    public void testInvalidLocalPart() {
        Nip05Validator validator = Nip05Validator.builder()
                .nip05("bad!part@example.com")
                .publicKey("pub")
                .build();
        assertThrows(NostrException.class, validator::validate);
    }

    @Test
    public void testUnknownDomain() {
        Nip05Validator validator = Nip05Validator.builder()
                .nip05("user@http://example.com")
                .publicKey("pub")
                .build();
        assertThrows(NostrException.class, validator::validate);
    }

    @Test
    public void testGetPublicKeyViaReflection() throws Exception {
        Nip05Validator validator = Nip05Validator.builder()
                .nip05("user@example.com")
                .publicKey("pub")
                .build();
        Method m = Nip05Validator.class.getDeclaredMethod("getPublicKey", StringBuilder.class, String.class);
        m.setAccessible(true);
        String json = "{\"names\":{\"alice\":\"abc\"}}";
        String result = (String) m.invoke(validator, new StringBuilder(json), "alice");
        assertEquals("abc", result);
        String missing = (String) m.invoke(validator, new StringBuilder(json), "bob");
        assertNull(missing);
    }
}
