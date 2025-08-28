package nostr.util.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class HexStringValidatorTest {

  @Test
  public void testValidHexString() {
    assertDoesNotThrow(() -> HexStringValidator.validateHex("deadbeef", 8));
  }

  @Test
  public void testInvalidLength() {
    assertThrows(IllegalArgumentException.class, () -> HexStringValidator.validateHex("abc", 4));
  }

  @Test
  public void testInvalidCharacters() {
    assertThrows(IllegalArgumentException.class, () -> HexStringValidator.validateHex("zzzz", 4));
  }

  @Test
  public void testUpperCaseCharacters() {
    assertThrows(IllegalArgumentException.class, () -> HexStringValidator.validateHex("ABcd", 4));
  }
}
