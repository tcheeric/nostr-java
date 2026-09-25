package nostr.mcp.argument;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies tool arguments are read without changing what the caller meant. */
class ToolArgumentsTest {

  // Verifies an empty tag value keeps its position, since NIP-01 tags are positional and
  // dropping the empty relay hint in ["p", pk, "", "mention"] moves the marker into its place.
  @Test
  void nestedTextsKeepEmptyValuesInPlace() {
    var arguments = new ToolArguments(Map.of("tags", List.of(List.of("p", "abc", "", "mention"))));

    assertEquals(List.of(List.of("p", "abc", "", "mention")), arguments.nestedTexts("tags"));
  }

  // Verifies tag values are not trimmed, since a tag can carry quoted text that must go out
  // byte-for-byte.
  @Test
  void nestedTextsDoNotTrim() {
    var arguments = new ToolArguments(Map.of("tags", List.of(List.of("comment", " spaced \n"))));

    assertEquals(List.of(List.of("comment", " spaced \n")), arguments.nestedTexts("tags"));
  }

  // Verifies a single flat tag is read the same way, since that path is taken when a model sends
  // only the inner array.
  @Test
  void aFlatTagKeepsEmptyValuesInPlace() {
    var arguments = new ToolArguments(Map.of("tags", List.of("p", "abc", "", "mention")));

    assertEquals(List.of(List.of("p", "abc", "", "mention")), arguments.nestedTexts("tags"));
  }
}
