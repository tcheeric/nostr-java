package nostr.context.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.Relay;
import nostr.context.CommandContext;
import nostr.event.BaseMessage;

@Data
@NoArgsConstructor
public class DefaultCommandContext implements CommandContext {
    private BaseMessage message;
    private String challenge;
    private Relay relay;
    private byte[] privateKey;

    @Override
    public void validate() {

    }
}
