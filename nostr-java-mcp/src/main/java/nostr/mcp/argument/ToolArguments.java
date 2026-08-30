package nostr.mcp.argument;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads a tool call's arguments without every tool repeating the same casts.
 *
 * <p>MCP delivers arguments as an untyped map, so a tool that reads them directly is a tool full
 * of unchecked casts, each of which fails as a {@code ClassCastException} the agent cannot act
 * on. Reading through here turns a wrong type into an {@code INVALID_ARGUMENT} that names the
 * argument and says what was expected.
 *
 * <p>A model will also send a number as a string, or a single value where a list is allowed,
 * because JSON schemas are a suggestion to a language model rather than a constraint. Coercing
 * those here is the difference between a tool that works with real agents and one that is
 * correct on paper.
 */
public final class ToolArguments {

  private final Map<String, Object> arguments;

  /**
   * @param arguments the raw arguments from the tool call, which may be null
   */
  public ToolArguments(Map<String, Object> arguments) {
    this.arguments = arguments == null ? Map.of() : arguments;
  }

  /**
   * Read a required text argument.
   *
   * @param name the argument to read
   * @return its value
   * @throws nostr.mcp.tool.ToolException when it is absent or blank
   */
  public String requireText(@NonNull String name) {
    return text(name)
        .orElseThrow(() -> ToolFailure.INVALID_ARGUMENT.raise("'" + name + "' is required"));
  }

  /**
   * Read an optional text argument.
   *
   * @param name the argument to read
   * @return its value, or empty when absent or blank
   */
  public Optional<String> text(@NonNull String name) {
    Object value = arguments.get(name);
    if (value == null) {
      return Optional.empty();
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? Optional.empty() : Optional.of(text);
  }

  /**
   * Read an optional whole-number argument.
   *
   * @param name the argument to read
   * @return its value, or empty when absent
   * @throws nostr.mcp.tool.ToolException when present but not a whole number
   */
  public Optional<Integer> integer(@NonNull String name) {
    Optional<String> text = text(name);
    if (text.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of((int) Double.parseDouble(text.get()));
    } catch (NumberFormatException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + name + "' should be a number but was '" + text.get() + "'");
    }
  }

  /**
   * Read a list argument, accepting a single value in place of a one-element list.
   *
   * @param name the argument to read
   * @return the values, empty when absent
   */
  public List<String> texts(@NonNull String name) {
    Object value = arguments.get(name);
    return switch (value) {
      case null -> List.of();
      case List<?> list -> list.stream().map(String::valueOf).map(String::trim).filter(text -> !text.isEmpty()).toList();
      default -> text(name).map(List::of).orElseGet(List::of);
    };
  }

  /**
   * Read a list of whole numbers, accepting a single value in place of a list.
   *
   * @param name the argument to read
   * @return the values, empty when absent
   * @throws nostr.mcp.tool.ToolException when any value is not a whole number
   */
  public List<Integer> integers(@NonNull String name) {
    return texts(name).stream().map(text -> parseInteger(name, text)).toList();
  }

  /**
   * Read the tag filters, which NIP-01 writes as single-letter keys.
   *
   * @param name the argument holding them
   * @return each tag letter and the values to match
   */
  public Map<String, List<String>> tagFilters(@NonNull String name) {
    Object value = arguments.get(name);
    if (!(value instanceof Map<?, ?> map)) {
      return Map.of();
    }
    return map.entrySet().stream()
        .collect(
            java.util.stream.Collectors.toMap(
                entry -> String.valueOf(entry.getKey()),
                entry -> new ToolArguments(Map.of("v", entry.getValue())).texts("v"),
                (first, second) -> first,
                java.util.LinkedHashMap::new));
  }

  /**
   * Read a list of lists, as NIP-01 writes tags.
   *
   * <p>A single flat list is read as one entry, since a model given an example of nested arrays
   * will sometimes send just the inner one.
   *
   * @param name the argument to read
   * @return each inner list's values
   */
  public List<List<String>> nestedTexts(@NonNull String name) {
    Object value = arguments.get(name);
    if (!(value instanceof List<?> outer) || outer.isEmpty()) {
      return List.of();
    }
    if (outer.stream().noneMatch(List.class::isInstance)) {
      return List.of(texts(name));
    }
    return outer.stream()
        .filter(List.class::isInstance)
        .map(inner -> new ToolArguments(Map.of("v", inner)).texts("v"))
        .toList();
  }

  private int parseInteger(String name, String text) {
    try {
      return (int) Double.parseDouble(text);
    } catch (NumberFormatException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + name + "' should contain only numbers but had '" + text + "'");
    }
  }
}
