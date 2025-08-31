package nostr.util.validator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;

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
@Data
@Slf4j
public class Nip05Validator {

    private final String nip05;
    private final String publicKey;

    @Builder
    public Nip05Validator(String nip05, String publicKey) {
        this.nip05 = nip05;
        this.publicKey = publicKey;
    }
    
    // Reuse a single HttpClient instance (HttpClient is not Closeable)
    private transient volatile HttpClient cachedClient;
    
    private HttpClient client() {
        HttpClient local = cachedClient;
        if (local == null) {
            synchronized (this) {
                if (cachedClient == null) {
                    cachedClient = HttpClient.newHttpClient();
                }
                local = cachedClient;
            }
        }
        return local;
    }

    private static final String LOCAL_PART_PATTERN = "^[a-zA-Z0-9-_\\.]+$";

    //    TODO: refactor
    public void validate() throws NostrException {
        if (this.nip05 != null) {
            var splited = nip05.split("@");
            var localPart = splited[0];
            var domain = splited[1];

            if (!localPart.matches(LOCAL_PART_PATTERN)) {
                throw new NostrException("Invalid <local-part> syntax in nip05 attribute.");
            }

            // Verify the public key
            try {
                log.debug("Validating {}@{}", localPart, domain);
                validatePublicKey(domain, localPart);
            } catch (URISyntaxException ex) {
                log.error("Validation error", ex);
                throw new NostrException(ex);
            }
        }
    }
    String[] split = nip05.trim().split("@");
    if (split.length != 2) {
      throw new NostrException("Invalid nip05 identifier format.");
    }
    String localPart = split[0].trim();
    String domainPart = split[1].trim();

    //    TODO: refactor
    private void validatePublicKey(String domain, String localPart) throws NostrException, URISyntaxException {

        String strUrl = "https://<domain>/.well-known/nostr.json?name=<localPart>"
                .replace("<domain>", domain)
                .replace("<localPart>", localPart);

        HttpClient client = client();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(strUrl))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("HTTP request error", ex);
            throw new NostrException(String.format("Failed to connect to %s: %s", strUrl, ex.getMessage()));
        }

        if (response.statusCode() == 200) {
            StringBuilder content = new StringBuilder(response.body());

            String pubKey = getPublicKey(content, localPart);
            log.debug("Public key for {} returned by the server: [{}]", localPart, pubKey);

            if (pubKey != null && !pubKey.equals(publicKey)) {
                throw new NostrException(String.format("Public key mismatch. Expected %s - Received: %s", publicKey, pubKey));
            }
            return;
        }

        throw new NostrException(String.format("Failed to connect to %s. Status: %d", strUrl, response.statusCode()));
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
        throw new NostrException("Invalid port in domain.", ex);
      }
      if (port < 0 || port > 65535) {
        throw new NostrException("Invalid port in domain.");
      }
    }

    validatePublicKey(host, port, localPart);
  }

  private void validatePublicKey(String host, int port, String localPart) throws NostrException {
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
      throw new NostrException("Invalid URI for host " + host + ": " + ex.getMessage(), ex);
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
      throw new NostrException(String.format("Error querying %s: %s", uri, ex.getMessage()), ex);
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
      throw new NostrException("Invalid NIP-05 response: " + ex.getMessage(), ex);
    }

    Map<String, String> names = nip05Content.getNames();
    if (names == null) {
      return null;
    }
    return names.get(localPart);
  }
}
