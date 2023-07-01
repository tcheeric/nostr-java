package nostr.event.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.event.Nip05Content;
import nostr.event.json.codec.Nip05ContentDecoder;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@Log
public class Nip05Validator {

    private final String nip05;
    private final PublicKey publicKey;
    
    private static final String LOCAL_PART_PATTERN = "^[a-zA-Z0-9-_\\.]+$";

    public void validate() throws NostrException {
        if (this.nip05 != null) {
            var localPart = nip05.split("@")[0];
            var domain = nip05.split("@")[1];

            if (!localPart.matches(LOCAL_PART_PATTERN)) {
                throw new NostrException("Invalid <local-part> syntax in nip05 attribute.");
            }

            // Verify the public key
            try {
                validatePublicKey(domain, localPart);
            } catch (IOException ex) {
                throw new NostrException(ex);
            }
        }
    }

    private void validatePublicKey(String domain, String localPart) throws MalformedURLException, NostrException, IOException, ProtocolException {

        // Set up and estgetPublicKeyablish the HTTP connection
        String strUrl = "https://<domain>/.well-known/nostr.json?name=<localPart>".replace("<domain>", domain).replace("<localPart>", localPart);
        URL url = new URL(strUrl);
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
            log.log(Level.INFO, "Public key for {0} returned by the server: [{1}]", new Object[]{localPart, pubKey});

            if (pubKey != null && !pubKey.equals(publicKey.toString())) {
                throw new NostrException(String.format("Public key mismatch. Expected {0} - Received: {1}", new Object[]{publicKey.toString(), pubKey}));
            }

            // All well!
            return;
        }

        throw new NostrException(String.format("Failed to connect to {0}. Error message: {1)", new Object[]{strUrl, connection.getResponseMessage()}));
    }

    private String getPublicKey(StringBuilder content, String localPart) throws NostrException {

        Nip05Content nip05Content = new Nip05ContentDecoder(content.toString()).decode();

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
}
