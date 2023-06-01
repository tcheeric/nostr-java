/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.base.Command;
import nostr.event.impl.GenericMessage;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class CloseMessage extends GenericMessage {

    private final String subscriptionId;

    public CloseMessage(String subscriptionId) {
        super(Command.CLOSE.name());
        this.subscriptionId = subscriptionId;
    }
}
