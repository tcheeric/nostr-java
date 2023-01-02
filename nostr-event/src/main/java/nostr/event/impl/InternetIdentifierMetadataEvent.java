package nostr.event.impl;

import nostr.base.Profile;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.list.TagList;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import java.beans.IntrospectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.types.values.IValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
@Event(name = "Internet Identifier Metadata Event", nip = 5)
public final class InternetIdentifierMetadataEvent extends GenericEvent {

    private final String name;
    private final String nip05;

    public InternetIdentifierMetadataEvent(PublicKey pubKey, TagList tags, @NonNull Profile profile) throws NostrException, NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        super(pubKey, Kind.SET_METADATA, tags);
        this.name = profile.getName();
        this.nip05 = profile.getEmail();
    }

    @Override
    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this.validateNip05();

        setContent();

        super.update();

    }

    private void setContent() {
        IValue nameValue = new StringValue(this.name);
        ExpressionValue nameExpr = new ExpressionValue("name", nameValue);

        IValue nip05Value = new StringValue(this.nip05);
        ExpressionValue nip05Expr = new ExpressionValue("nip05", nip05Value);

        List<ExpressionValue> expressions = new ArrayList<>();
        expressions.add(nameExpr);
        expressions.add(nip05Expr);
        
        ObjectValue content = new ObjectValue(expressions);

        setContent(content.toString());
    }

    private void validateNip05() throws NostrException {
        if (this.nip05 != null) {
            var localPart = nip05.split("@")[0];
            var domain = nip05.split("@")[1];

            if (!localPart.matches("^[-\\w.]+$")) {
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

        // Set up and establish the HTTP connection
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

            if (!pubKey.equals(getPubKey().toString())) {
                throw new NostrException(String.format("Public key mismatch. Expected {0} - Received: {1}", new Object[]{getPubKey().toString(), pubKey}));
            }
        }

        throw new NostrException(String.format("Failed to connect to {0}. Error message: {1)", new Object[]{strUrl, connection.getResponseMessage()}));
    }

    private String getPublicKey(StringBuilder content, String localPart) {
        ObjectValue jsonObjValue = new JsonObjectUnmarshaller(content.toString()).unmarshall();
        IValue namesObj = ((ObjectValue) jsonObjValue).get("\"" + "names" + "\"");
        IValue pubKey = ((ObjectValue) namesObj).get("\"" + localPart + "\"");
        return pubKey.getValue().toString();
    }

}
