/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event;

import nostr.base.Command;
import nostr.base.IElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
@ToString
public abstract class BaseMessage implements IElement {

    private final Command command;
}
