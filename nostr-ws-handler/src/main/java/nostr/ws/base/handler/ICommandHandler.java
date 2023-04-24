/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package nostr.ws.base.handler;

import nostr.base.Command;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public interface ICommandHandler extends IHandler {
    
    public void process(Object... params) throws NostrException;
    
    public Command getCommand();
}
