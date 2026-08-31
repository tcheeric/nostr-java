package nostr.mcp.tool;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.NonNull;
import nostr.event.filter.EventFilter;
import nostr.event.impl.Contact;
import nostr.event.impl.ContactList;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.NostrIdentifier;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.query.QueryResult;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads who someone follows.
 *
 * <p>A follow list is how an agent answers "what are the people I follow saying", which is the
 * question behind most feed-shaped requests. Kind 3 is replaceable, so several relays may hold
 * different generations and the newest wins.
 *
 * <p>Each entry keeps its relay hint and petname rather than only the key, because those are how
 * a client finds someone it has never seen and shows a readable name without a global registry.
 */
public final class GetContactsTool implements NostrTool {

  private static final int CONTACT_LIST_KIND = 3;

  private final EventQuery eventQuery;
  private final IdentityVault identityVault;
  private final QueryLimits limits;

  /**
   * @param eventQuery finds the list
   * @param identityVault resolves whose list to read when none is named
   * @param limits the configured query bounds
   */
  public GetContactsTool(
      @NonNull EventQuery eventQuery,
      @NonNull IdentityVault identityVault,
      @NonNull QueryLimits limits) {
    this.eventQuery = eventQuery;
    this.identityVault = identityVault;
    this.limits = limits;
  }

  @Override
  public String name() {
    return "nostr_get_contacts";
  }

  @Override
  public String description() {
    return "Read who someone follows (NIP-02). Defaults to this server's own identity.";
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
                "Whose follow list to read, as hex or npub. Omit for your own.")),
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
                .author(owner)
                .kind(CONTACT_LIST_KIND)
                .limit(limits.maxEventsPerQuery())
                .build(),
            limits.maxEventsPerQuery(),
            limits.queryTimeout());

    Optional<ContactList> contacts =
        found.events().stream()
            .max(Comparator.comparing(GenericEvent::getCreatedAt))
            .map(ContactList::from);

    return contacts
        .map(list -> describe(owner, list))
        .orElseGet(() -> noList(owner, found));
  }

  private CallToolResult describe(String owner, ContactList contacts) {
    List<Map<String, Object>> described = contacts.getContacts().stream().map(GetContactsTool::describe).toList();
    return CallToolResult.builder()
        .structuredContent(Map.of("pubkey", owner, "contacts", described, "count", described.size()))
        .addTextContent("Follows " + described.size() + " account(s).")
        .build();
  }

  private static Map<String, Object> describe(Contact contact) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("pubkey", contact.getPublicKey().toHexString());
    described.put("npub", contact.getPublicKey().toBech32String());
    described.put("relay", contact.findRelay().map(nostr.base.Relay::getUri).orElse(""));
    described.put("petname", contact.findPetname().orElse(""));
    return described;
  }

  /**
   * Reports an absent list as an answer, distinguishing it from an unfinished lookup.
   *
   * <p>Most keys have never published a follow list, which is a fact about the person rather
   * than a failure, but a timed-out query is not the same thing and saying so prevents an agent
   * concluding that somebody follows nobody.
   */
  private CallToolResult noList(String owner, QueryResult found) {
    return CallToolResult.builder()
        .structuredContent(Map.of("pubkey", owner, "contacts", List.of(), "found", false))
        .addTextContent(
            found.timedOut()
                ? "No follow list arrived before the query timed out, so this key may have one."
                : "This key has published no follow list on the configured relays.")
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
                                    + " contacts could be meant.")));
  }
}
