/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.message;

import nostr.event.BaseMessage;
import nostr.base.Command;
import nostr.event.impl.GenericEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class EventMessage extends BaseMessage {

    private final GenericEvent event;

    public EventMessage(@NonNull GenericEvent event) {
        super(Command.EVENT);
        this.event = event;
    }
}
