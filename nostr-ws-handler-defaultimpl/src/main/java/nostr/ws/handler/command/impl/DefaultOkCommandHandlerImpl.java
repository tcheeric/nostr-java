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
import nostr.ws.base.handler.command.IOkCommandHandler;

/**
 *
 * @author eric
 */
@Data
@Log
public class DefaultOkCommandHandlerImpl implements IOkCommandHandler {

    private String eventId;
    private String message;
    private Reason reason;
    private boolean result;
    private Relay relay;

    @Override
    public void process(Object... params) throws NostrException {
        String _eventId = (String) params[0];
        String _message = (String) params[1];
        Reason _reason = Reason.valueOf((String) params[2]);
        Boolean _result = Boolean.valueOf((String) params[3]);
        Relay _relay = (Relay) params[4];

        this.setEventId(_eventId);
        this.setMessage(_message);
        this.setReason(_reason);
        this.setResult(_result);
        this.setRelay(_relay);
    }

    @Override
    public Command getCommand() {
        return Command.OK;
    }
}
