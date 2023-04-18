package nostr.test.crypto;

import nostr.crypto.bech32.Bech32;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import nostr.base.Bech32Prefix;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author squirrel
 */
public class CryptoTest {

    @Test
    public void testBech32() {
        try {
            System.out.println("testBech32");

            final String hexPub = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
            final String npub = "npub126klq89p42wk78p4j5ur8wlxmxdqepdh8tez9e4axpd4run5nahsmff27j";

            final var hrp = Bech32Prefix.NPUB.getCode();            

            Assertions.assertEquals(npub, Bech32.toBech32(hrp, hexPub));
            Assertions.assertEquals("56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f", Bech32.fromBech32(npub));
        } catch (NostrException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Assertions.fail(ex);
        }
    }
}
