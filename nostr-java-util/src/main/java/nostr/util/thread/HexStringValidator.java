package nostr.util.thread;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class HexStringValidator {
    private static final String validHexChars = "0123456789abcdef";

    private static final BiPredicate<String, Integer> lengthCheck = (s, l) -> Objects.equals(s.length(), l);
    private static final Predicate<String> hexCharsCheck = HexStringValidator::validHex;
    private static final Predicate<String> upperCaseCheck = s -> s.toLowerCase().equals(s);

    public static void validateHex(@NonNull String hexString, int targetLength) {
        // split into distinct checks per unique/specific error message
        assert lengthCheck.test(hexString, targetLength) :
                String.format("Invalid hex string: [%s], length: [%d], target length: [%d]", hexString, hexString.length(), targetLength);
        assert hexCharsCheck.test(hexString) :
                String.format("Invalid hex string: [%s] has non-hex characters", hexString);
        assert upperCaseCheck.test(hexString) :
                String.format("Invalid hex string: [%s] has upper-case characters", hexString);
    }

    private static Boolean validHex(String aHexString) {
        return StringUtils.containsOnly(aHexString.toLowerCase(), validHexChars);
    }
}
