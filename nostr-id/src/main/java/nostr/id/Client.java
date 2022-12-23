/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.id;

import nostr.base.BaseConfiguration;
import nostr.util.NostrUtil;
import nostr.base.Relay;
import nostr.controller.Connection;
import nostr.controller.handler.request.RequestHandler;
import nostr.event.BaseMessage;
import nostr.json.JsonValue;
import nostr.json.values.JsonArrayValue;
import nostr.json.values.JsonNumberValue;
import nostr.json.values.JsonObjectValue;
import nostr.json.types.JsonObjectType;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Log
@Data
@ToString
public class Client {

    @ToString.Exclude
    private final Set<Relay> relays;

    private final String name;

    @ToString.Include
    private final Wallet wallet;

    public Client(@NonNull String name, String relayConfFile, @NonNull Wallet wallet) throws IOException {
        this.relays = new HashSet<>();
        this.name = name;
        this.wallet = wallet;

        this.init(relayConfFile);
    }

    public Client(@NonNull String name, @NonNull Wallet wallet) throws IOException {
        this(name, "/relays.properties", wallet);
    }

    public void send(@NonNull BaseMessage message) throws IOException, Exception {
        for (Relay r : relays) {
            var rh = RequestHandler.builder().connection(new Connection(r)).message(message).build();
            
            log.log(Level.INFO, "Client {0} sending message to {1}", new Object[]{this, r});
            rh.process();
        }
    }

    private void addRelay(@NonNull Relay relay) {
        this.relays.add(relay);
        updateRelayInformation(relay);
        log.log(Level.FINE, "Added relay {0}", relay);
    }

    private void init(String file) throws IOException {
        List<Relay> relayList = new RelayConfiguration(file).getRelays();
        for (Relay r : relayList) {
            this.addRelay(r);
        }
    }

    private void updateRelayInformation(@NonNull Relay relay) {
        try {
            var connection = new Connection(relay);
            String strInfo = connection.getRelayInformation();
            log.log(Level.FINE, "Relay information: {0}", strInfo);
            JsonValue<JsonObjectType> info = new JsonObjectUnmarshaller(strInfo).unmarshall();

            final JsonValue contact = ((JsonObjectValue) info).get("\"contact\"");
            var strContact = contact == null ? "" : contact.toString();
            relay.setContact(strContact);

            final JsonValue desc = ((JsonObjectValue) info).get("\"description\"");
            var strDesc = desc == null ? "" : desc.toString();
            relay.setDescription(strDesc);

            final JsonValue relayName = ((JsonObjectValue) info).get("\"name\"");
            var strRelayName = relayName == null ? "" : relayName.toString();
            relay.setName(strRelayName);

            final JsonValue software = ((JsonObjectValue) info).get("\"software\"");
            var strSoftware = software == null ? "" : software.toString();
            relay.setSoftware(strSoftware);

            final JsonValue version = ((JsonObjectValue) info).get("\"version\"");
            var strVersion = version == null ? "" : version.toString();
            relay.setVersion(strVersion);

            List<Integer> snipList = new ArrayList<>();
            JsonArrayValue snips = (JsonArrayValue) ((JsonObjectValue) info).get("\"supported_nips\"");
            int len = snips.length();
            for (int i = 0; i < len; i++) {
                snipList.add(((JsonNumberValue) snips.get(i)).intValue());
            }
            relay.setSupportedNips(snipList);

            final JsonValue pubKey = ((JsonObjectValue) info).get("\"pubkey\"");
            var strPubKey = pubKey == null ? "" : pubKey.toString();
            relay.setPubKey(NostrUtil.hexToBytes(strPubKey));
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    static class RelayConfiguration extends BaseConfiguration {

        RelayConfiguration() throws IOException {
            this("/relays.properties");
        }

        RelayConfiguration(String file) throws IOException {
            super(file);
        }

        List<Relay> getRelays() {
            Set<Object> relays = this.properties.keySet();
            List<Relay> result = new ArrayList<>();

            for (Object r : relays) {
                Relay relay = Relay.builder().name(r.toString()).uri(this.getProperty(r.toString())).build();
                result.add(relay);
            }
            return result;
        }
    }
}
