package nostr.mcp.argument;

import lombok.NonNull;
import nostr.event.filter.EventFilter;

import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * Turns tool arguments into a NIP-01 filter.
 *
 * <p>Querying and subscribing take the same filter, so the schema and the decoding live here
 * rather than being written twice and drifting. An agent that has learnt to filter for one can
 * use the other unchanged, which is worth more than either tool's independence.
 */
public final class EventFilterArguments {

  private EventFilterArguments() {}

  /**
   * The schema shared by every filtering tool.
   *
   * @return the JSON schema for a filter
   */
  public static Map<String, Object> schema() {
    return schemaWith(Map.of());
  }

  /**
   * The shared filter schema plus a tool's own arguments.
   *
   * @param additionalProperties properties this tool adds, such as a result limit
   * @return the combined JSON schema
   */
  public static Map<String, Object> schemaWith(@NonNull Map<String, Object> additionalProperties) {
    Map<String, Object> properties = new java.util.LinkedHashMap<>(filterProperties());
    properties.putAll(additionalProperties);
    return Map.of("type", "object", "properties", properties, "required", List.of());
  }

  private static Map<String, Object> filterProperties() {
    return Map.of(
            "authors",
                Map.of(
                    "type", "array",
                    "description", "Public keys to match, as hex or npub.",
                    "items", Map.of("type", "string")),
            "kinds",
                Map.of(
                    "type", "array",
                    "description", "Event kinds to match, such as 1 for notes.",
                    "items", Map.of("type", "integer")),
            "ids",
                Map.of(
                    "type", "array",
                    "description", "Event ids to match, as hex, note or nevent.",
                    "items", Map.of("type", "string")),
            "tags",
                Map.of(
                    "type",
                    "object",
                    "description",
                    "Tag filters keyed by tag letter, such as {\"p\": [\"<pubkey>\"]}."),
            "since", Map.of("type", "string", "description", "Only events at or after this time."),
            "until", Map.of("type", "string", "description", "Only events before this time."));
  }

  /**
   * Decode a filter from a call's arguments.
   *
   * @param arguments the caller's arguments
   * @param clock what "now" means for relative times
   * @return the filter
   * @throws nostr.mcp.tool.ToolException when an argument is malformed
   */
  public static EventFilter toFilter(@NonNull ToolArguments arguments, @NonNull Clock clock) {
    return toFilterBuilder(arguments, clock).build();
  }

  /**
   * Decode a filter, leaving it open for a tool to add its own terms.
   *
   * @param arguments the caller's arguments
   * @param clock what "now" means for relative times
   * @return the part-built filter
   * @throws nostr.mcp.tool.ToolException when an argument is malformed
   */
  public static EventFilter.Builder toFilterBuilder(
      @NonNull ToolArguments arguments, @NonNull Clock clock) {
    EventFilter.Builder filter = EventFilter.builder();
    arguments.texts("authors").stream()
        .map(author -> NostrIdentifier.publicKey("authors", author).hex())
        .forEach(filter::author);
    arguments.texts("ids").stream()
        .map(id -> NostrIdentifier.eventId("ids", id).hex())
        .forEach(filter::id);
    arguments.integers("kinds").forEach(filter::kind);
    arguments.tagFilters("tags").forEach(filter::addTagFilter);
    arguments
        .text("since")
        .ifPresent(since -> filter.since(TimeArgument.toUnixSeconds("since", since, clock)));
    arguments
        .text("until")
        .ifPresent(until -> filter.until(TimeArgument.toUnixSeconds("until", until, clock)));
    return filter;
  }
}
