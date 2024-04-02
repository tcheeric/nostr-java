package nostr.context.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.context.RequestContext;
import nostr.event.BaseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class DefaultRequestContext implements RequestContext {
    private byte[] privateKey;
    private BaseMessage message;
    private String subscriptionId;
    private String challenge;
    private Map<String, String> relays;
    private List<String> relayConnections = new ArrayList<>();

    public void addConnection(String relay) {
        this.relayConnections.add(relay);
    }

    public void removeConnection(String relay) {
        this.relayConnections.remove(relay);
    }

    public boolean isConnected(String relay) {
        return this.relayConnections.contains(relay);
    }

    @Override
    public void validate() {
    }
}