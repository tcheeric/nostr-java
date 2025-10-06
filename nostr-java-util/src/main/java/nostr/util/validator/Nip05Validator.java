package nostr.util.validator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import nostr.util.http.DefaultHttpClientProvider;
import nostr.util.http.HttpClientProvider;

/**
 * Validator for NIP-05 identifiers.
 *
 * @author squirrel
 */
@Builder
@Data
@Slf4j
@lombok.AllArgsConstructor
public class Nip05Validator {

  private final String nip05;
  private final String publicKey;
  @Builder.Default @JsonIgnore private final Duration connectTimeout = Duration.ofSeconds(5);
  @Builder.Default @JsonIgnore private final Duration requestTimeout = Duration.ofSeconds(5);

  @Builder.Default @JsonIgnore
  private final HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();

  private static final Pattern LOCAL_PART_PATTERN = Pattern.compile("^[a-zA-Z0-9-_.]+$");
  private static final Pattern DOMAIN_PATTERN = Pattern.compile("^[A-Za-z0-9.-]+(:\\d{1,5})?$");
  private static final ObjectMapper MAPPER_BLACKBIRD =
      JsonMapper.builder().addModule(new BlackbirdModule()).build();


  /**
   * Validate the nip05 identifier by checking the public key registered on the remote server.
   *
   * @throws NostrException if validation fails
   */
  public void validate() throws NostrException {
    if (this.nip05 == null) {
      return;
    }
    String[] split = nip05.trim().split("@");
    if (split.length != 2) {
      throw new NostrException("Invalid nip05 identifier format.");
    }
    String localPart = split[0].trim();
    String domainPart = split[1].trim();

    if (!LOCAL_PART_PATTERN.matcher(localPart).matches()) {
      throw new NostrException("Invalid <local-part> syntax in nip05 attribute.");
    }
    if (!DOMAIN_PATTERN.matcher(domainPart).matches()) {
      throw new NostrException("Invalid domain syntax in nip05 attribute.");
    }

    localPart = localPart.toLowerCase(Locale.ROOT);
    String host;
    int port = -1;
    String[] hostPort = domainPart.split(":", 2);
    host = IDN.toASCII(hostPort[0].toLowerCase(Locale.ROOT));
    if (hostPort.length == 2) {
      try {
        port = Integer.parseInt(hostPort[1]);
      } catch (NumberFormatException ex) {
        throw new NostrException("Invalid port in domain.");
      }
      if (port < 0 || port > 65535) {
        throw new NostrException("Invalid port in domain.");
      }
    }

    validatePublicKey(host, port, localPart);
  }

  private void validatePublicKey(String host, int port, String localPart) throws NostrException {
    @SuppressWarnings("resource")
    HttpClient client = httpClientProvider.create(connectTimeout);

    URI uri;
    try {
      uri =
          new URI(
              "https",
              null,
              host,
              port,
              "/.well-known/nostr.json",
              "name=" + URLEncoder.encode(localPart, StandardCharsets.UTF_8),
              null);
    } catch (URISyntaxException ex) {
      log.error("Validation error", ex);
      throw new NostrException("Invalid URI for host " + host + ": " + ex.getMessage());
    }

    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().timeout(requestTimeout).build();

    HttpResponse<String> response;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException ex) {
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      log.error("HTTP request error", ex);
      throw new NostrException(String.format("Error querying %s: %s", uri, ex.getMessage()));
    }

    if (response.statusCode() != 200) {
      throw new NostrException(
          String.format("Unexpected HTTP status %d from %s", response.statusCode(), uri));
    }

    String pubKey = getPublicKey(response.body(), localPart);
    log.debug("Public key for {} returned by the server: [{}]", localPart, pubKey);

    if (pubKey == null) {
      throw new NostrException(String.format("No NIP-05 record for '%s' at %s", localPart, uri));
    }
    if (!pubKey.equals(publicKey)) {
      throw new NostrException(
          String.format("Public key mismatch. Expected %s - Received: %s", publicKey, pubKey));
    }
  }

  private String getPublicKey(String content, String localPart) throws NostrException {
    Nip05Content nip05Content;
    try {
      nip05Content = MAPPER_BLACKBIRD.readValue(content, Nip05Content.class);
    } catch (IOException ex) {
      throw new NostrException("Invalid NIP-05 response: " + ex.getMessage());
    }

    Map<String, String> names = nip05Content.getNames();
    if (names == null) {
      return null;
    }
    return names.get(localPart);
  }
}
