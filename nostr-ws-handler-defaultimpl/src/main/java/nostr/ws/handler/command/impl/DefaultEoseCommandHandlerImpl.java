/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.ws.handler.command.impl;

import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.Relay;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.util.NostrException;
import nostr.ws.base.handler.command.IEoseCommandHandler;

/**
 *
 * @author eric
 */
@Data
@Log
public class DefaultEoseCommandHandlerImpl implements IEoseCommandHandler {

    private String subscriptionId;
    
    @Override
    public void process(Object... params) throws NostrException {
        String message = (String) params[0];
        Relay relay = (Relay) params[1];
        process(message, relay);
    }

    private void process(String message, Relay relay) throws NostrException {
        ArrayValue jsonArr = new JsonArrayUnmarshaller(message).unmarshall();
        
        String _subscriptionId = (jsonArr).get(1).get().getValue().toString();

        this.setSubscriptionId(_subscriptionId);
    }

    @Override
    public Command getCommand() {
        return Command.EOSE;
    }

}
