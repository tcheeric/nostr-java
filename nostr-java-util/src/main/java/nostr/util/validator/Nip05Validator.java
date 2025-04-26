package nostr.util.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import nostr.util.NostrException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@Log
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
                log.log(Level.FINE, "Validating {0}@{1}", new Object[]{localPart, domain});
                validatePublicKey(domain, localPart);
            } catch (IOException | URISyntaxException ex) {
                log.log(Level.SEVERE, ex.getMessage());
                throw new NostrException(ex);
            }
        }
    }

    //    TODO: refactor
    private void validatePublicKey(String domain, String localPart) throws NostrException, IOException, URISyntaxException {

        // Set up and estgetPublicKeyablish the HTTP connection
        String strUrl = "https://<domain>/.well-known/nostr.json?name=<localPart>".replace("<domain>", domain).replace("<localPart>", localPart);
        URL url = new URI(strUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Read the connection response (1) and validate (2)
        if (connection.getResponseCode() == 200) { // (1)
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // (2)
            String pubKey = getPublicKey(content, localPart);
            log.log(Level.FINE, "Public key for {0} returned by the server: [{1}]", new Object[]{localPart, pubKey});

            if (pubKey != null && !pubKey.equals(publicKey)) {
                throw new NostrException(String.format("Public key mismatch. Expected %s - Received: %s", publicKey, pubKey));
            }

            // All well!
            return;
        }

        throw new NostrException(String.format("Failed to connect to %s. Error message: %s", strUrl, connection.getResponseMessage()));
    }

    @SneakyThrows
    private String getPublicKey(StringBuilder content, String localPart) {

        ObjectMapper MAPPER_AFTERBURNER = JsonMapper.builder().addModule(new AfterburnerModule()).build();
        Nip05Content nip05Content = MAPPER_AFTERBURNER.readValue(content.toString(), Nip05Content.class);

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
