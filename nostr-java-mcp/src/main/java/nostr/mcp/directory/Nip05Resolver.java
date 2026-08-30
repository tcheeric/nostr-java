package nostr.mcp.directory;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Turns a human-readable NIP-05 address into the public key behind it.
 *
 * <p>People know each other as {@code alice@example.com}, not as 64 hex characters, so an agent
 * asked about someone by name has an address and needs a key. The SDK's {@code Nip05Validator}
 * answers the opposite question, checking an address against a key already known, so resolution
 * lives here.
 *
 * <p>Resolution is deliberately not verification. A domain's answer proves the domain claims that
 * key, which is the whole guarantee NIP-05 offers, and callers should not read more into it.
 */
public final class Nip05Resolver {

  private static final String WELL_KNOWN_PATH = "/.well-known/nostr.json";
  private static final String IMPLIED_LOCAL_PART = "_";

  private final WellKnownJson wellKnownJson;

  /**
   * @param wellKnownJson fetches the domain's document
   */
  public Nip05Resolver(@NonNull WellKnownJson wellKnownJson) {
    this.wellKnownJson = wellKnownJson;
  }

  /**
   * Resolve an address to a public key.
   *
   * @param address a NIP-05 address such as {@code alice@example.com}, or a bare domain
   * @return the public key in hex
   * @throws nostr.mcp.tool.ToolException when the address is malformed or the domain has no
   *     record for it
   */
  public String resolve(@NonNull String address) {
    String normalised = address.trim().toLowerCase(Locale.ROOT);
    String localPart = localPartOf(normalised);
    String domain = domainOf(normalised);
    JsonNode names = wellKnownJson.fetch(uriFor(domain, localPart), "application/json", address).path("names");
    JsonNode publicKey = names.path(localPart);
    if (publicKey.isMissingNode() || !publicKey.isTextual()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          domain + " publishes no NIP-05 record for '" + localPart + "'");
    }
    return publicKey.asText();
  }

  /**
   * Reads the implied local part of a bare domain.
   *
   * <p>NIP-05 defines {@code example.com} as shorthand for {@code _@example.com}, and a user who
   * types the short form means the long one.
   */
  private String localPartOf(String address) {
    int separator = address.indexOf('@');
    return separator < 0 ? IMPLIED_LOCAL_PART : address.substring(0, separator);
  }

  private String domainOf(String address) {
    int separator = address.indexOf('@');
    String domain = separator < 0 ? address : address.substring(separator + 1);
    if (domain.isEmpty() || !domain.contains(".")) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "'" + address + "' is not a NIP-05 address; expected something like alice@example.com");
    }
    return domain;
  }

  private URI uriFor(String domain, String localPart) {
    return URI.create(
        "https://"
            + domain
            + WELL_KNOWN_PATH
            + "?name="
            + URLEncoder.encode(localPart, StandardCharsets.UTF_8));
  }
}
