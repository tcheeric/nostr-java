package nostr.util.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 *
 * @author squirrel
 */
@Builder
@RequiredArgsConstructor
@Data
@Slf4j
public class Nip05Validator {

    private final String nip05;
    private final String publicKey;

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

    //    TODO: refactor
    private void validatePublicKey(String domain, String localPart) throws NostrException, URISyntaxException {

        String strUrl = "https://<domain>/.well-known/nostr.json?name=<localPart>"
                .replace("<domain>", domain)
                .replace("<localPart>", localPart);

        HttpClient client = HttpClient.newHttpClient();
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

    @SneakyThrows
    private String getPublicKey(StringBuilder content, String localPart) {

        ObjectMapper MAPPER_BLACKBIRD = JsonMapper.builder().addModule(new BlackbirdModule()).build();
        Nip05Content nip05Content = MAPPER_BLACKBIRD.readValue(content.toString(), Nip05Content.class);

        // Access the decoded data
        Map<String, String> names = nip05Content.getNames();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            String name = entry.getKey();
            String hash = entry.getValue();
            if (name.equals(localPart)) {
                return hash;
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    public static final class Nip05Obj {
        private String name;
        private String nip05;
    }

}
