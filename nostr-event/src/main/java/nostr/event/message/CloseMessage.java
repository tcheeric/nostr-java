/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.message;

import nostr.event.BaseMessage;
import nostr.base.Command;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class CloseMessage extends BaseMessage {

    private final String subscriptionId;

    public CloseMessage(String subscriptionId) {
        super(Command.CLOSE);
        this.subscriptionId = subscriptionId;
    }
}
