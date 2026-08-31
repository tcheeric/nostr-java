package nostr.mcp.relay;

import lombok.NonNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the relay names an agent uses into the URIs the SDK connects to.
 *
 * <p>An agent should be able to say "publish to my write relays" without knowing a websocket
 * URI, and a deployment should be able to change where that points without the agent noticing.
 * This holds that mapping and nothing else.
 *
 * <p>It deliberately owns no connections. Those belong to the SDK's {@code RelayPool}, and a
 * second thing in the codebase called a relay pool would be a trap for anyone reading either.
 */
public final class RelayDirectory {

  /** The logical name for relays events are published to. */
  public static final String WRITE = "write";

  /** The logical name for relays events are read from. */
  public static final String READ = "read";

  private final Map<String, List<String>> relaysByName;

  /**
   * @param relaysByName logical names mapped to the relay URIs they stand for
   */
  public RelayDirectory(@NonNull Map<String, List<String>> relaysByName) {
    this.relaysByName = Map.copyOf(relaysByName);
  }

  /**
   * Resolve names or URIs into relay URIs.
   *
   * <p>A caller may pass either, since an agent naturally writes {@code "write"} while a
   * configuration file names a specific relay. Anything not a known name is taken to be a URI,
   * which keeps a one-off relay usable without registering it first.
   *
   * @param namesOrUris logical names, relay URIs, or a mixture
   * @return the resolved URIs, deduplicated, in the order given
   */
  public List<String> resolve(@NonNull List<String> namesOrUris) {
    Set<String> resolved = new LinkedHashSet<>();
    namesOrUris.forEach(
        nameOrUri -> resolved.addAll(relaysByName.getOrDefault(nameOrUri, List.of(nameOrUri))));
    return List.copyOf(resolved);
  }

  /**
   * Every relay this directory knows about, whatever name it was registered under.
   *
   * @return the distinct relay URIs
   */
  public List<String> allRelayUris() {
    Set<String> all = new LinkedHashSet<>();
    relaysByName.values().forEach(all::addAll);
    return List.copyOf(all);
  }

  /**
   * The logical names an agent can use.
   *
   * @return the registered names
   */
  public Set<String> names() {
    return relaysByName.keySet();
  }
}
