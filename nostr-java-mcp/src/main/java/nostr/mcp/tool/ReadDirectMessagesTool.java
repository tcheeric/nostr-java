package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.TimeArgument;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.social.McpDirectMessageService;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads private messages addressed to one identity.
 *
 * <p>Gift wraps are addressed to a single-use key and carry a randomised timestamp, so they are
 * found by the {@code p} tag naming the recipient rather than by author or time. Unwrapping is
 * attempted for each, and one that cannot be opened is skipped rather than failing the call: a
 * relay will happily return wraps addressed to somebody else.
 *
 * <p>Decryption is opt-in per identity. Reading correspondence into a conversation puts it in
 * the host's logs and probably a third-party API, so it stays a decision for the person whose
 * messages they are.
 */
public final class ReadDirectMessagesTool implements NostrTool {

  private static final int GIFT_WRAP_KIND = 1059;
  private static final String RECIPIENT_TAG = "p";

  private final McpDirectMessageService directMessages;
  private final IdentityVault identityVault;
  private final EventQuery eventQuery;
  private final QueryLimits limits;
  private final Clock clock;

  /**
   * @param directMessages unwraps the messages
   * @param identityVault resolves which identity to read for
   * @param eventQuery finds the wraps
   * @param limits the configured query bounds
   * @param clock what "now" means for relative times
   */
  public ReadDirectMessagesTool(
      @NonNull McpDirectMessageService directMessages,
      @NonNull IdentityVault identityVault,
      @NonNull EventQuery eventQuery,
      @NonNull QueryLimits limits,
      @NonNull Clock clock) {
    this.directMessages = directMessages;
    this.identityVault = identityVault;
    this.eventQuery = eventQuery;
    this.limits = limits;
    this.clock = clock;
  }

  @Override
  public String name() {
    return "nostr_read_direct_messages";
  }

  @Override
  public String description() {
    return "Read private messages sent to one of this server's identities. Must be enabled per"
        + " identity, since it brings private correspondence into this conversation.";
  }

  /**
   * A bound server omits {@code identity}, since it reads for exactly one.
   */
  @Override
  public Map<String, Object> inputSchema() {
    Map<String, Object> properties = new java.util.LinkedHashMap<>();
    if (!identityVault.binding().isBound()) {
      properties.put(
          "identity", Map.of("type", "string", "description", "Alias to read for. Omit for the default."));
    }
    properties.put(
        "since",
        Map.of(
            "type",
            "string",
            "description",
            "Only messages received after this time, such as '24h'. Note that gift wraps carry"
                + " randomised timestamps, so this is approximate."));
    return Map.of("type", "object", "properties", properties, "required", List.of());
  }

  @Override
  public CallToolResult call(CallToolRequest request) {
    try {
      return read(new ToolArguments(request.arguments()));
    } catch (ToolException e) {
      return e.asResult();
    }
  }

  private CallToolResult read(ToolArguments arguments) {
    String alias = resolveIdentity(arguments);
    if (!directMessages.mayDecrypt(alias)) {
      throw ToolFailure.WRITE_FORBIDDEN.raise(
          "Reading '"
              + alias
              + "' private messages is not enabled on this server. Start it with"
              + " -Dnostr.mcp.dm.decrypt-for=" + alias + " if the owner wants that.");
    }
    List<Map<String, Object>> messages = unwrap(alias, findWraps(alias, arguments));
    return CallToolResult.builder()
        .structuredContent(Map.of("messages", messages, "count", messages.size()))
        .addTextContent(
            messages.isEmpty()
                ? "No private messages found for '" + alias + "'."
                : "Found " + messages.size() + " private message(s) for '" + alias + "'.")
        .build();
  }

  private List<GenericEvent> findWraps(String alias, ToolArguments arguments) {
    EventFilter.Builder filter =
        EventFilter.builder()
            .kind(GIFT_WRAP_KIND)
            .addTagFilter(RECIPIENT_TAG, directMessages.publicKeyOf(alias).toHexString())
            .limit(limits.maxEventsPerQuery());
    arguments
        .text("since")
        .ifPresent(since -> filter.since(TimeArgument.toUnixSeconds("since", since, clock)));
    return eventQuery.run(filter.build(), limits.maxEventsPerQuery(), limits.queryTimeout()).events();
  }

  /**
   * Opens each wrap, skipping any that will not open.
   *
   * <p>A relay returns whatever matches the tag, including wraps this identity cannot decrypt,
   * so failing on the first would make the tool unusable rather than reporting a real problem.
   */
  private List<Map<String, Object>> unwrap(String alias, List<GenericEvent> wraps) {
    return wraps.stream()
        .map(wrap -> tryUnwrap(alias, wrap))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  private Map<String, Object> tryUnwrap(String alias, GenericEvent wrap) {
    try {
      ChatMessage message = directMessages.read(alias, wrap);
      Map<String, Object> described = new LinkedHashMap<>();
      described.put("from", message.getSender() == null ? null : message.getSender().toHexString());
      described.put("content", message.getContent());
      described.put("receivedWrapId", wrap.getId());
      return described;
    } catch (RuntimeException notForUs) {
      return null;
    }
  }

  private String resolveIdentity(ToolArguments arguments) {
    return arguments
        .text("identity")
        .orElseGet(
            () ->
                identityVault
                    .defaultAlias()
                    .orElseThrow(
                        () ->
                            ToolFailure.IDENTITY_AMBIGUOUS.raise(
                                "This server holds several identities and none is the default."
                                    + " Name one in 'identity'.")));
  }
}
