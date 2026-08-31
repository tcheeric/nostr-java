package nostr.mcp.guidance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import lombok.NonNull;
import nostr.mcp.identity.IdentitySummary;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.relay.RelayDirectory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes the server's own configuration as readable resources.
 *
 * <p>An agent needs to know which identity it is and which relays it can reach before it does
 * anything useful, and asking through a tool call spends a turn on something that never changes
 * during a conversation. A host can read these once and put them in context, which is what
 * resources are for.
 *
 * <p>They carry only what is already public: aliases, public keys and relay URIs. No private key
 * appears here for the same reason it appears in no tool result.
 */
public final class ContextResources {

  private static final String IDENTITY_URI_PREFIX = "nostr://identity/";
  private static final String RELAY_URI_PREFIX = "nostr://relay/";
  private static final String MEDIA_TYPE = "application/json";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ContextResources() {}

  /**
   * The resources describing this server's identities and relays.
   *
   * @param identityVault the identities this server holds
   * @param relayDirectory the relays it is configured with
   * @return the resource specifications to register
   */
  public static List<SyncResourceSpecification> all(
      @NonNull IdentityVault identityVault, @NonNull RelayDirectory relayDirectory) {
    return List.of(identityResource(identityVault), relayResource(relayDirectory));
  }

  private static SyncResourceSpecification identityResource(IdentityVault identityVault) {
    Resource resource =
        Resource.builder()
            .uri(IDENTITY_URI_PREFIX + "{alias}")
            .name("Nostr identity")
            .description("An identity this server can sign with: its alias, public key and npub.")
            .mimeType(MEDIA_TYPE)
            .build();
    return new SyncResourceSpecification(
        resource,
        (exchange, request) -> readIdentity(identityVault, request.uri()));
  }

  private static ReadResourceResult readIdentity(IdentityVault identityVault, String uri) {
    String alias = lastSegmentOf(uri);
    return identityVault
        .find(alias)
        .map(summary -> contents(uri, describe(summary, identityVault)))
        .orElseGet(() -> contents(uri, Map.of("error", "no identity called '" + alias + "'")));
  }

  private static Map<String, Object> describe(IdentitySummary summary, IdentityVault identityVault) {
    Map<String, Object> described = new LinkedHashMap<>();
    described.put("alias", summary.alias());
    described.put("publicKey", summary.publicKey());
    described.put("npub", summary.npub());
    described.put("isDefault", identityVault.defaultAlias().map(summary.alias()::equals).orElse(false));
    return described;
  }

  private static SyncResourceSpecification relayResource(RelayDirectory relayDirectory) {
    Resource resource =
        Resource.builder()
            .uri(RELAY_URI_PREFIX + "{name}")
            .name("Nostr relay")
            .description("A relay this server is configured to use.")
            .mimeType(MEDIA_TYPE)
            .build();
    return new SyncResourceSpecification(
        resource, (exchange, request) -> readRelay(relayDirectory, request.uri()));
  }

  private static ReadResourceResult readRelay(RelayDirectory relayDirectory, String uri) {
    String name = lastSegmentOf(uri);
    List<String> resolved = relayDirectory.resolve(List.of(name));
    return contents(
        uri,
        resolved.isEmpty()
            ? Map.of("error", "no relay called '" + name + "'", "known", relayDirectory.names())
            : Map.of("name", name, "relays", resolved));
  }

  private static String lastSegmentOf(String uri) {
    return uri.substring(uri.lastIndexOf('/') + 1);
  }

  private static ReadResourceResult contents(String uri, Map<String, Object> fields) {
    return new ReadResourceResult(List.of(new TextResourceContents(uri, MEDIA_TYPE, asJson(fields))));
  }

  private static String asJson(Map<String, Object> fields) {
    try {
      return MAPPER.writeValueAsString(fields);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
