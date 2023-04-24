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
import nostr.ws.base.handler.command.IAuthCommandHandler;

/**
 *
 * @author eric
 */
@Data
@Log
public class DefaultAuthCommandHandlerImpl implements IAuthCommandHandler {

    private String challenge;
    private Relay relay;    

    @Override
    public void process(Object... params) throws NostrException {
        String _challenge = (String) params[0];
        Relay _relay = (Relay) params[1];
        
        this.setChallenge(_challenge);
        this.setRelay(_relay);
    }

    @Override
    public Command getCommand() {
        return Command.AUTH;
    }
    
}
