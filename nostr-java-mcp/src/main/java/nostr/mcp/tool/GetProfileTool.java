package nostr.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.directory.Nip05Resolver;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.query.QueryResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Answers "who is this?" for a public key or a NIP-05 address.
 *
 * <p>A profile is a kind-0 event whose content is a JSON string, so reading one means a query, a
 * newest-wins choice, and a nested parse. Doing that in a dedicated tool keeps three awkward
 * steps out of every agent's reasoning, and the encoding is the kind of detail a model gets
 * subtly wrong.
 */
public final class GetProfileTool implements NostrTool {

  private static final int PROFILE_KIND = 0;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final EventQuery eventQuery;
  private final Nip05Resolver nip05Resolver;
  private final QueryLimits limits;

  /**
   * @param eventQuery runs the lookup
   * @param nip05Resolver turns an address into a key
   * @param limits the configured query bounds
   */
  public GetProfileTool(
      @NonNull EventQuery eventQuery,
      @NonNull Nip05Resolver nip05Resolver,
      @NonNull QueryLimits limits) {
    this.eventQuery = eventQuery;
    this.nip05Resolver = nip05Resolver;
    this.limits = limits;
  }

  @Override
  public String name() {
    return "nostr_get_profile";
  }

  @Override
  public String description() {
    return "Look up someone's profile metadata by public key (hex or npub) or NIP-05 address"
        + " such as alice@example.com.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "pubkey", Map.of("type", "string", "description", "The public key, as hex or npub."),
            "nip05",
                Map.of(
                    "type",
                    "string",
                    "description",
                    "A NIP-05 address such as alice@example.com, used when no pubkey is given.")));
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return answer(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult answer(ToolArguments arguments) {
    String publicKey = resolveSubject(arguments);
    QueryResult result =
        eventQuery.run(
            EventFilter.builder().author(publicKey).kind(PROFILE_KIND).limit(limits.maxEventsPerQuery()).build(),
            limits.maxEventsPerQuery(),
            limits.queryTimeout());

    return newestOf(result)
        .map(profile -> found(publicKey, profile))
        .orElseGet(() -> notFound(publicKey, result));
  }

  /**
   * Takes the subject from whichever argument the caller used.
   *
   * <p>Requiring exactly one is deliberate: given both, honouring one silently would answer a
   * question the caller did not ask if the two disagree.
   */
  private String resolveSubject(ToolArguments arguments) {
    Optional<String> pubkey = arguments.text("pubkey");
    Optional<String> nip05 = arguments.text("nip05");
    if (pubkey.isPresent() && nip05.isPresent()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "Give either 'pubkey' or 'nip05', not both, so the answer is unambiguous");
    }
    if (pubkey.isPresent()) {
      return NostrIdentifier.publicKey("pubkey", pubkey.get()).hex();
    }
    return nip05Resolver.resolve(
        nip05.orElseThrow(
            () -> ToolFailure.INVALID_ARGUMENT.raise("Give either 'pubkey' or 'nip05'")));
  }

  /**
   * Chooses the newest profile event.
   *
   * <p>Kind 0 is replaceable, so several relays may return different generations of the same
   * profile. Newest wins, which is what a client would show.
   */
  private Optional<GenericEvent> newestOf(QueryResult result) {
    return result.events().stream()
        .max(java.util.Comparator.comparing(GenericEvent::getCreatedAt));
  }

  private CallToolResult found(String publicKey, GenericEvent profile) {
    Map<String, Object> fields = parseContent(profile.getContent());
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("pubkey", publicKey);
    structured.put("npub", new nostr.base.PublicKey(publicKey).toBech32String());
    structured.put("updatedAt", profile.getCreatedAt());
    structured.putAll(fields);
    return CallToolResult.builder()
        .structuredContent(structured)
        .addTextContent(summarise(fields, publicKey))
        .build();
  }

  /**
   * Reports an absent profile as an answer, not an error.
   *
   * <p>Most keys have no kind-0 event, which is a fact about the person rather than a failure of
   * the lookup. A timed-out query is different and says so, because "no profile" and "I did not
   * finish looking" would otherwise be indistinguishable.
   */
  private CallToolResult notFound(String publicKey, QueryResult result) {
    String detail =
        result.timedOut()
            ? "No profile arrived before the query timed out, so this key may still have one."
            : "No profile has been published for this key on the configured relays.";
    return CallToolResult.builder()
        .structuredContent(Map.of("pubkey", publicKey, "found", false, "timedOut", result.timedOut()))
        .addTextContent(detail)
        .build();
  }

  /**
   * Reads the profile's JSON content, tolerating a malformed one.
   *
   * <p>Anyone can publish anything as kind 0, so a broken profile is a fact about the network,
   * not an error in the lookup. Returning the raw content lets the agent see what was there
   * instead of the tool failing on someone else's mistake.
   */
  private Map<String, Object> parseContent(String content) {
    if (content == null || content.isBlank()) {
      return Map.of();
    }
    try {
      JsonNode parsed = MAPPER.readTree(content);
      if (!parsed.isObject()) {
        return Map.of("rawContent", content);
      }
      Map<String, Object> fields = new LinkedHashMap<>();
      parsed.properties().forEach(entry -> fields.put(entry.getKey(), asPlainValue(entry.getValue())));
      return fields;
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return Map.of("rawContent", content);
    }
  }

  private Object asPlainValue(JsonNode value) {
    return value.isTextual() ? value.asText() : value.toString();
  }

  private String summarise(Map<String, Object> fields, String publicKey) {
    Object name = fields.getOrDefault("display_name", fields.get("name"));
    if (name == null) {
      return "A profile exists for " + publicKey + " but it names nobody.";
    }
    Object about = fields.get("about");
    return about == null ? String.valueOf(name) : name + ": " + about;
  }
}
