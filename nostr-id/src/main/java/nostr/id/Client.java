package nostr.id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.BaseConfiguration;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericMessage;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.types.values.IValue;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.NumberValue;
import nostr.types.values.impl.ObjectValue;
import nostr.util.NostrUtil;
import nostr.ws.Connection;
import nostr.ws.handler.request.DefaultRequestHandler;

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
    private final Identity identity;

    public Client(@NonNull String name, String relayConfFile, @NonNull Identity identity) throws IOException {
        this.relays = new HashSet<>();
        this.name = name;
        this.identity = identity;

        this.init(relayConfFile);
    }

    public Client(@NonNull String name, @NonNull Identity identity, Map<String, String> relays) throws IOException {
        this.relays = new HashSet<>();
        this.name = name;
        this.identity = identity;

        this.init(relays);
    }

    public void send(@NonNull GenericMessage message) {

        relays.parallelStream()
                .filter(r -> r.getSupportedNips().contains(message.getNip()))
                .forEach(r -> {
                    try {
                        var rh = DefaultRequestHandler.builder().connection(new Connection(r)).message(message).build();
                        log.log(Level.INFO, "Client {0} sending message to {1}", new Object[]{this, r});
                        rh.process();
                    } catch (Exception ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                });
    }

    private void addRelay(@NonNull Relay relay) {
        this.relays.add(relay);
        updateRelayInformation(relay);
        log.log(Level.FINE, "Added relay {0}", relay);
    }

    private void init(String file) throws IOException {
        List<Relay> relayList = new RelayConfiguration(file).getRelays();
        relayList.stream().forEach(r -> this.addRelay(r));
    }

    private void init(Map<String, String> mapRelays) {
        for (Map.Entry<String, String> r : mapRelays.entrySet()) {
            this.addRelay(Relay.builder().name(r.getKey()).uri(r.getValue()).build());
        }

    }

    private void updateRelayInformation(@NonNull Relay relay) {
        try {
            var connection = new Connection(relay);
            String strInfo = connection.getRelayInformation();
            log.log(Level.FINE, "Relay information: {0}", strInfo);
            ObjectValue info = new JsonObjectUnmarshaller(strInfo).unmarshall();

            if (((ObjectValue) info).get("contact").isPresent()) {
                final IValue contact = ((ObjectValue) info).get("contact").get();
                var strContact = contact == null ? "" : contact.toString();
                relay.setContact(strContact);
            }

            if (((ObjectValue) info).get("description").isPresent()) {
                final IValue desc = ((ObjectValue) info).get("description").get();
                var strDesc = desc == null ? "" : desc.toString();
                relay.setDescription(strDesc);
            }

            if (((ObjectValue) info).get("name").isPresent()) {
                final IValue relayName = ((ObjectValue) info).get("name").get();
                var strRelayName = relayName == null ? "" : relayName.toString();
                relay.setName(strRelayName);
            }

            if (((ObjectValue) info).get("software").isPresent()) {
                final IValue software = ((ObjectValue) info).get("software").get();
                var strSoftware = software == null ? "" : software.toString();
                relay.setSoftware(strSoftware);
            }

            if (((ObjectValue) info).get("version").isPresent()) {
                final IValue version = ((ObjectValue) info).get("version").get();
                var strVersion = version == null ? "" : version.toString();
                relay.setVersion(strVersion);
            }

            if (((ObjectValue) info).get("supported_nips").isPresent()) {
                List<Integer> snipList = new ArrayList<>();
                ArrayValue snips = (ArrayValue) ((ObjectValue) info).get("supported_nips").get();
                int len = snips.length();
                for (int i = 0; i < len; i++) {
                    snipList.add(((NumberValue) snips.get(i).get()).intValue());
                }
                relay.setSupportedNips(snipList);
            }

            if (((ObjectValue) info).get("pubkey").isPresent()) {
                final IValue pubKey = ((ObjectValue) info).get("pubkey").get();
                var strPubKey = pubKey == null ? "" : pubKey.toString();
                relay.setPubKey(new PublicKey(NostrUtil.hexToBytes(strPubKey)));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @Log
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

            relays.stream().forEach(r -> {                
                var relay = Relay.builder().name(r.toString()).uri(this.getProperty(r.toString())).build();
                result.add(relay);
            });
            return result;
        }
    }
}
