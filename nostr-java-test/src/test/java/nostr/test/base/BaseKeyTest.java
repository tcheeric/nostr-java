package nostr.test.base;

import nostr.base.PublicKey;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseKeyTest {
    public static final String VALID_HEXPUBKEY = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    public static final String INVALID_HEXPUBKEY_NON_HEX_DIGITS = "XYZdf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
    public static final String INVALID_HEXPUBKEY_LENGTH_TOO_SHORT = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6";
    public static final String INVALID_HEXPUBKEY_LENGTH_TOO_LONG = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f666";
    public static final String VALID_HEXPUBKEY_ALL_ZEROS = "0000000000000000000000000000000000000000000000000000000000000000";
    public static final String VALID_HEXPUBKEY_ALL_FF = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    public static final String INVALID_HEXPUBKEY_HAS_MULTIPLE_UPPERCASE = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
    public static final String INVALID_HEXPUBKEY_HAS_SINGLE_UPPERCASE = "56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6F";

    @Test
    public void testValidPublicKeyString() {
        System.out.println("testValidPublicKeyString");
        assertDoesNotThrow(() -> new PublicKey(VALID_HEXPUBKEY));
    }

    @Test
    public void testValidPublicKeyByteArray() {
        System.out.println("testValidPublicKeyByteArray");
        assertDoesNotThrow(() -> new PublicKey(VALID_HEXPUBKEY.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testInValidNullPublicKeyString() {
        System.out.println("testInValidNullPublicKeyString");
        assertThrows(AssertionError.class, () -> new PublicKey(""));
    }

    @Test
    public void testInValidPublicKeyNonHexDigits() {
        System.out.println("testInValidPublicKeyNonHexDigits");
        assertThrows(AssertionError.class, () -> new PublicKey(INVALID_HEXPUBKEY_NON_HEX_DIGITS));
    }

    @Test
    public void testInValidPublicKeyLengthTooShort() {
        System.out.println("testInValidPublicKeyLengthTooShort");
        assertThrows(AssertionError.class, () -> new PublicKey(INVALID_HEXPUBKEY_LENGTH_TOO_SHORT));
    }

    @Test
    public void testInValidPublicKeyLengthTooLong() {
        System.out.println("testInValidPublicKeyLengthTooShort");
        assertThrows(AssertionError.class, () -> new PublicKey(INVALID_HEXPUBKEY_LENGTH_TOO_LONG));
    }

    @Test
    public void testValidPublicKeyAllZeros() {
        System.out.println("testValidPublicKeyAllZeros");
        assertDoesNotThrow(() -> new PublicKey(VALID_HEXPUBKEY_ALL_ZEROS));
    }

    @Test
    public void testValidPublicKeyAllFF() {
        System.out.println("testValidPublicKeyAllFF");
        assertDoesNotThrow(() -> new PublicKey(VALID_HEXPUBKEY_ALL_FF));
    }

    @Test
    public void testInvalidPublicKeyMultipleUppercase() {
        System.out.println("testInvalidPublicKeyMultipleUppercase");
        assertThrows(AssertionError.class, () -> new PublicKey(INVALID_HEXPUBKEY_HAS_MULTIPLE_UPPERCASE));
    }

    @Test
    public void testInvalidPublicKeySingleUppercase() {
        System.out.println("testInvalidPublicKeySingleUppercase");
        assertThrows(AssertionError.class, () -> new PublicKey(INVALID_HEXPUBKEY_HAS_SINGLE_UPPERCASE));
    }
}
