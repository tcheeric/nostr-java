package nostr.reqsp.impl;

import lombok.Data;
import nostr.event.BaseMessage;
import nostr.reqsp.Request;
import nostr.ws.Connection;

@Data
public class DefaultRequest implements Request {

    private Connection connection;
    private String subscriptionId;
    private BaseMessage message;

}
