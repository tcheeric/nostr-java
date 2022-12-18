
package com.tcheeric.nostr.controller.handler.response;

import com.tcheeric.nostr.base.Command;
import com.tcheeric.nostr.controller.IHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@AllArgsConstructor
public abstract class BaseResponseHandler implements IHandler {
    
    private final Command command;
    
}
