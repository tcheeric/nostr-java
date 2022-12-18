
package com.tcheeric.nostr.event.marshaller;

import com.tcheeric.nostr.base.NostrException;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {
    
    public abstract String marshall() throws NostrException;
}
