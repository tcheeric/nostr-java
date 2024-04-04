package nostr.context.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.context.RequestContext;

import java.util.Map;

@Data
@NoArgsConstructor
public class DefaultRequestContext implements RequestContext {
    private byte[] privateKey;
    private String subscriptionId;
    private String challenge;
    private Map<String, String> relays;

    @Override
    public void validate() {
    }
}