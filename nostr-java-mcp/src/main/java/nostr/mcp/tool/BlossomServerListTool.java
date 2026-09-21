package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.blossom.BlossomServers;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.query.QueryResult;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Reads which Blossom servers someone uses.
 *
 * <p>BUD-03 publishes this as a kind-10063 event, which makes a user's media hosting
 * discoverable the same way their relays are. It is how an agent finds where to look for
 * someone else's media, and where to put its own without the operator hard-coding a server.
 *
 * <p>Order is meaningful and preserved: the spec has the author list their most trusted server
 * first, and clients are expected to try them in that order.
 */
public final class BlossomServerListTool implements NostrTool {

  /** The kind BUD-03 reserves for a user's server list. */
  public static final int SERVER_LIST_KIND = 10_063;

  private static final String SERVER_TAG = "server";

  private final EventQuery eventQuery;
  private final IdentityVault identityVault;
  private final QueryLimits limits;

  /**
   * @param eventQuery finds the list
   * @param identityVault resolves whose list is meant when none is named
   * @param limits the configured query bounds
   */
  public BlossomServerListTool(
      @NonNull EventQuery eventQuery,
      @NonNull IdentityVault identityVault,
      @NonNull QueryLimits limits) {
    this.eventQuery = eventQuery;
    this.identityVault = identityVault;
    this.limits = limits;
  }

  @Override
  public String name() {
    return "nostr_blossom_get_servers";
  }

  @Override
  public String description() {
    return "Read the Blossom media servers a public key publishes (BUD-03, kind 10063), most"
        + " trusted first. Defaults to this server's own identity.";
  }

  @Override
  public Map<String, Object> inputSchema() {
    return Map.of(
        "type",
        "object",
        "properties",
        Map.of(
            "pubkey",
            Map.of(
                "type",
                "string",
                "description",
                "Whose server list to read, as hex or npub. Omit for this server's own key.")),
        "required",
        List.of());
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
    String owner = resolveOwner(arguments);
    QueryResult found =
        eventQuery.run(
            EventFilter.builder()
                .kind(SERVER_LIST_KIND)
                .author(owner)
                .limit(limits.maxEventsPerQuery())
                .build(),
            limits.maxEventsPerQuery(),
            limits.queryTimeout());

    return found.events().stream()
        .max(Comparator.comparing(GenericEvent::getCreatedAt))
        .map(event -> describe(owner, serversIn(event)))
        .orElseGet(() -> noList(owner, found));
  }

  /**
   * Pulls the server URLs out of a list event, keeping the author's order.
   *
   * <p>Anything that is not a {@code server} tag is ignored rather than refused: BUD-03 leaves
   * room for other tags, and a list carrying one should still be readable.
   */
  private List<String> serversIn(GenericEvent event) {
    return BlossomServers.fromServerTags(
        event.getTags().stream()
            .filter(tag -> SERVER_TAG.equals(tag.getCode()))
            .filter(GenericTag.class::isInstance)
            .map(GenericTag.class::cast)
            .map(GenericTag::getParams)
            .filter(params -> !params.isEmpty())
            .map(params -> params.get(0))
            .toList());
  }

  private CallToolResult describe(String owner, List<String> servers) {
    return CallToolResult.builder()
        .structuredContent(
            Map.of("pubkey", owner, "servers", servers, "count", servers.size(), "found", true))
        .addTextContent(
            servers.isEmpty()
                ? "That key published a server list with no servers in it."
                : "Blossom servers, most trusted first: " + String.join(", ", servers) + ".")
        .build();
  }

  /**
   * Reports an absent list, distinguishing it from a lookup that did not finish.
   *
   * <p>Most keys have never published one, which is a fact about the person rather than a
   * failure. A timed-out query is not the same thing, and saying so stops an agent concluding
   * somebody hosts nothing.
   */
  private CallToolResult noList(String owner, QueryResult found) {
    return CallToolResult.builder()
        .structuredContent(
            Map.of("pubkey", owner, "servers", List.of(), "count", 0, "found", false))
        .addTextContent(
            found.timedOut()
                ? "No server list arrived before the query timed out, so this key may have one."
                : "This key published no Blossom server list on the configured relays.")
        .build();
  }

  private String resolveOwner(ToolArguments arguments) {
    return arguments
        .text("pubkey")
        .map(pubkey -> NostrIdentifier.publicKey("pubkey", pubkey).hex())
        .orElseGet(
            () ->
                identityVault
                    .defaultAlias()
                    .map(alias -> identityVault.publicKeyOf(alias).toHexString())
                    .orElseThrow(
                        () ->
                            ToolFailure.IDENTITY_AMBIGUOUS.raise(
                                "Give a 'pubkey', since this server has no default identity whose"
                                    + " server list could be meant.")));
  }
}
