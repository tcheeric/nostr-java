package nostr.mcp.tool;

import lombok.NonNull;
import nostr.client.relay.RelayPool;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.directory.Nip05Resolver;
import nostr.mcp.directory.WellKnownJson;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.write.WriteGuard;
import nostr.mcp.write.WritePolicy;

import java.time.Clock;
import java.util.List;

/**
 * Builds the set of tools a server exposes, given how that server is configured.
 *
 * <p>The safety model is "a tool an agent cannot see is a tool it cannot misuse", which only
 * works if there is exactly one place that decides what is visible. This is that place, and it is
 * a separate type from the application so a test can assert the real surface instead of a
 * stand-in: a golden file over stub tools proves nothing about what a host actually receives.
 */
public final class ToolSurface {

  private ToolSurface() {}

  /**
   * Assemble the tools for a server.
   *
   * <p>Single-identity mode omits the keystore lifecycle tools entirely. A bound server operates
   * a key; it does not administer the keystore, and key administration belongs to the human who
   * set the servers up rather than to any agent.
   *
   * @param relayDirectory the configured relays
   * @param relayPool the live connections
   * @param identityVault the keys this server holds
   * @param queryLimits the bounds every read stays inside
   * @param clock what "now" means when resolving relative times
   * @param writeGuard the single point every write passes through
   * @param writePolicy whether write tools appear at all
   * @return the registry a host will see
   */
  public static NostrToolRegistry forServer(
      @NonNull RelayDirectory relayDirectory,
      @NonNull RelayPool relayPool,
      @NonNull IdentityVault identityVault,
      @NonNull QueryLimits queryLimits,
      @NonNull Clock clock,
      @NonNull WriteGuard writeGuard,
      @NonNull WritePolicy writePolicy) {
    EventQuery eventQuery = new EventQuery(relayPool);
    WellKnownJson wellKnownJson = new WellKnownJson();
    NostrToolRegistry registry =
        new NostrToolRegistry()
            .register(new ListRelaysTool(relayDirectory, relayPool))
            .register(new ListIdentitiesTool(identityVault))
            .register(new QueryEventsTool(eventQuery, queryLimits, clock))
            .register(new GetProfileTool(eventQuery, new Nip05Resolver(wellKnownJson), queryLimits))
            .register(new RelayInfoTool(relayDirectory, wellKnownJson));
    if (writePolicy.allowsWriteTools()) {
      writeTools(writeGuard).forEach(registry::register);
    }
    if (!identityVault.binding().isBound()) {
      administrationTools().forEach(registry::register);
    }
    return registry;
  }

  /**
   * The tools that mutate the keystore, registered only on an unbound server.
   *
   * <p>Empty until the lifecycle tools land, but the branch exists now so that binding is
   * enforced at the one point that decides visibility, rather than being retrofitted onto each
   * tool as it is added.
   *
   * @return the keystore-mutating tools
   */
  private static List<NostrTool> administrationTools() {
    return List.of();
  }

  /**
   * The tools that publish, registered only when the policy permits writing at all.
   *
   * <p>Under {@code write-policy: deny} they are absent rather than refusing, because a tool an
   * agent cannot see is a tool it cannot be talked into using. A refusal it can see is an
   * invitation to try a different phrasing.
   *
   * @param writeGuard the guard each write tool publishes through
   * @return the publishing tools
   */
  private static List<NostrTool> writeTools(WriteGuard writeGuard) {
    return List.of(
        new PublishNoteTool(writeGuard),
        new PublishEventTool(writeGuard),
        new UpdateProfileTool(writeGuard));
  }
}
