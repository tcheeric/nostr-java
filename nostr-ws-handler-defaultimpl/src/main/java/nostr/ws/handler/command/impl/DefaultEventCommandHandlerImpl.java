/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.ws.handler.command.impl;

import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.Relay;
import nostr.util.NostrException;
import nostr.ws.base.handler.command.IEventCommandHandler;

/**
 *
 * @author eric
 */
@Data
@Log
public class DefaultEventCommandHandlerImpl implements IEventCommandHandler {

    private String subscriptionId;
    private String jsonEvent;
    private Relay relay;
    
    @Override
    public void process(Object... params) throws NostrException {
        String _subscriptionId = (String) params[0];
        String _jsonEvent = (String) params[1];
        Relay _relay = (Relay) params[2];
        
        this.setSubscriptionId(_subscriptionId);
        this.setJsonEvent(_jsonEvent);
        this.setRelay(_relay);
    }

    @Override
    public Command getCommand() {
        return Command.EVENT;
    }
    
}
