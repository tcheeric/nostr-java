package nostr.context.impl;

import lombok.Data;
import nostr.base.Relay;
import nostr.context.Context;
import nostr.event.BaseMessage;

import java.util.List;

@Data
public class DefaultResponseContext implements Context {

    private String eventId;
    private String message;
    private boolean result;
    private String jsonEvent;
    private String challenge;
    private Relay relay;
    private String subscriptionId;
    private List<BaseMessage> responses;

    @Override
    public void validate() {

    }
}
