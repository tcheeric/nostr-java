package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Profile;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.list.TagList;
import com.tcheeric.nostr.json.JsonType;
import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.types.JsonObjectType;
import com.tcheeric.nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import com.tcheeric.nostr.json.values.JsonExpression;
import com.tcheeric.nostr.json.values.JsonObjectValue;
import com.tcheeric.nostr.json.values.JsonStringValue;
import com.tcheeric.nostr.json.values.JsonValueList;
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
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Log
@NIPSupport(value=5, description="Mapping Nostr keys to DNS-based internet identifiers")
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
        JsonValue nameValue = new JsonStringValue(this.name);
        JsonExpression<JsonType> nameExpr = JsonExpression.builder().variable("name").jsonValue(nameValue).build();

        JsonValue nip05Value = new JsonStringValue(this.nip05);
        JsonExpression<JsonType> nip05Expr = JsonExpression.builder().variable("nip05").jsonValue(nip05Value).build();

        JsonValueList value = new JsonValueList();
        value.add(nameExpr);
        value.add(nip05Expr);

        JsonObjectValue content = new JsonObjectValue(value);

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
        JsonValue<JsonObjectType> jsonObjValue = new JsonObjectUnmarshaller(content.toString()).unmarshall();
        JsonValue namesObj = ((JsonObjectValue) jsonObjValue).get("names");
        JsonValue pubKey = ((JsonObjectValue) namesObj).get(localPart);
        return pubKey.getValue().toString();
    }

}
