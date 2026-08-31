package nostr.mcp.tool;

import lombok.NonNull;
import nostr.client.relay.RelayPool;
import nostr.mcp.identity.IdentityLifecycle;
import nostr.mcp.identity.IdentityPolicy;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.directory.Nip05Resolver;
import nostr.mcp.directory.WellKnownJson;
import nostr.mcp.query.EventQuery;
import nostr.mcp.query.QueryLimits;
import nostr.mcp.relay.RelayDirectory;
import nostr.mcp.social.McpDirectMessageService;
import nostr.mcp.subscription.SubscriptionRegistry;
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
   * @param identityLifecycle performs keystore changes, or {@code null} when the backend cannot
   * @param identityPolicy whether keystore-mutating tools appear at all
   * @param subscriptions where open subscriptions live
   * @param directMessages sends and reads private messages
   * @return the registry a host will see
   */
  public static NostrToolRegistry forServer(
      @NonNull RelayDirectory relayDirectory,
      @NonNull RelayPool relayPool,
      @NonNull IdentityVault identityVault,
      @NonNull QueryLimits queryLimits,
      @NonNull Clock clock,
      @NonNull WriteGuard writeGuard,
      @NonNull WritePolicy writePolicy,
      IdentityLifecycle identityLifecycle,
      @NonNull IdentityPolicy identityPolicy,
      @NonNull SubscriptionRegistry subscriptions,
      @NonNull McpDirectMessageService directMessages) {
    EventQuery eventQuery = new EventQuery(relayPool);
    WellKnownJson wellKnownJson = new WellKnownJson();
    NostrToolRegistry registry =
        new NostrToolRegistry()
            .register(new ListRelaysTool(relayDirectory, relayPool))
            .register(new ListIdentitiesTool(identityVault))
            .register(new QueryEventsTool(eventQuery, queryLimits, clock))
            .register(new GetProfileTool(eventQuery, new Nip05Resolver(wellKnownJson), queryLimits))
            .register(new RelayInfoTool(relayDirectory, wellKnownJson))
            .register(new SubscribeTool(subscriptions, clock))
            .register(new ReadSubscriptionTool(subscriptions))
            .register(new ListSubscriptionsTool(subscriptions))
            .register(new UnsubscribeTool(subscriptions))
            .register(new FetchThreadTool(eventQuery, queryLimits))
            .register(new GetContactsTool(eventQuery, identityVault, queryLimits))
            .register(
                new ReadDirectMessagesTool(
                    directMessages, identityVault, eventQuery, queryLimits, clock));
    if (writePolicy.allowsWriteTools()) {
      writeTools(writeGuard).forEach(registry::register);
      registry.register(new SendDirectMessageTool(directMessages, identityVault));
    }
    if (registersAdministration(identityVault, identityPolicy, identityLifecycle)) {
      administrationTools(identityLifecycle, identityVault, identityPolicy).forEach(registry::register);
    }
    return registry;
  }

  /**
   * Whether this server administers its keystore at all.
   *
   * <p>Three separate reasons to say no, and each is a different question. A bound server
   * operates one key and does not administer a keystore. A denied policy forbids mutation. And a
   * backend that cannot be written to, such as a keychain on a host without one, has nothing to
   * offer, so registering tools that would always fail would be a worse answer than not offering
   * them.
   */
  private static boolean registersAdministration(
      IdentityVault identityVault, IdentityPolicy identityPolicy, IdentityLifecycle lifecycle) {
    return lifecycle != null
        && identityPolicy.allowsMutation()
        && !identityVault.binding().isBound();
  }

  /**
   * The tools that mutate the keystore.
   *
   * <p>Kept together so the whole dangerous half of the surface appears in one list: what an
   * agent can do to a user's keys should be readable at a glance rather than gathered from six
   * registration calls.
   */
  private static List<NostrTool> administrationTools(
      IdentityLifecycle lifecycle, IdentityVault identityVault, IdentityPolicy identityPolicy) {
    return List.of(
        new CreateIdentityTool(lifecycle),
        new ImportIdentityTool(lifecycle),
        new RenameIdentityTool(lifecycle),
        new SetDefaultIdentityTool(identityVault),
        new ExportIdentityBackupTool(lifecycle),
        new RemoveIdentityTool(lifecycle, identityVault, identityPolicy));
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
