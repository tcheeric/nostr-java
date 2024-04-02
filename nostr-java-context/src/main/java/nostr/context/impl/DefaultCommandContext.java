package nostr.context.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.Relay;
import nostr.context.CommandContext;

@Data
@NoArgsConstructor
public class DefaultCommandContext implements CommandContext {
    private String eventId;
    private String message;
    private boolean result;
    private String jsonEvent;
    private String challenge;
    private Relay relay;
    private String subscriptionId;
    private byte[] privateKey;

    @Override
    public void validate() {

    }
}
