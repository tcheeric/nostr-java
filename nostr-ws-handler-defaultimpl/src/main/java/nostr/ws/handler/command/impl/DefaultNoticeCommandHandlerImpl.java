/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.ws.handler.command.impl;

import lombok.Data;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.util.NostrException;
import nostr.ws.base.handler.command.INoticeCommandHandler;

/**
 *
 * @author eric
 */
@Data
@Log
public class DefaultNoticeCommandHandlerImpl implements INoticeCommandHandler {

    private String message;
    
    @Override
    public void process(Object... params) throws NostrException {
        String _message = (String) params[0];
        this.setMessage(_message);
    }

    @Override
    public Command getCommand() {
        return Command.NOTICE;
    }
    
}
