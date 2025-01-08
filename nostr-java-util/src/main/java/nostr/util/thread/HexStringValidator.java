package nostr.util.thread;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HexStringValidator {
  private static final String validHexChars = "0123456789abcdef";

  private static final BiFunction<String, Integer, Boolean> lengthCheck = (s, targetLength) -> s.length() == targetLength;
  private static final Function<String, Boolean> hexCharsCheck = HexStringValidator::checkValidHexChars;
  private static final Function<String, Boolean> upperCaseCheck = s -> s.toLowerCase().equals(s);

  public static void validateHex(@NonNull String hexString, int targetLength) {
    List<String> exceptions = new ArrayList<>();
    Optional.of(hexString) // non-null enforcement
        .filter(s -> {
          if (!lengthCheck.apply(s, targetLength)) {
            return exceptions.add(String.format("Invalid hex string: [%s], length: [%d], target length: [%d]", hexString, hexString.length(), targetLength));
          }
          return true;
        })
        .filter(s -> {
          if (!hexCharsCheck.apply(s)) {
            exceptions.add(String.format("Invalid hex string: [%s] has non-hex characters", hexString));
          }
          return true;
        })
        .filter(s -> {
          if (!upperCaseCheck.apply(s)) {
            exceptions.add(String.format("Invalid hex string: [%s] has uppcase characters", hexString));
          }
          return true;
        });

    if (!exceptions.isEmpty()) {
      throw new IllegalArgumentException(exceptions.getFirst());
    }
  }

  private static Boolean checkValidHexChars(String aHexString) {
    for (char a : aHexString.toLowerCase().toCharArray()) {
      if (validHexChars.indexOf(a) < 0)
        return false;
    }
    return true;
  }
}
