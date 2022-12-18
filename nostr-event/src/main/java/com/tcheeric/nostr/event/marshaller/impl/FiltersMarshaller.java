
package com.tcheeric.nostr.event.marshaller.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Relay;
import com.tcheeric.nostr.event.BaseMessage;
import com.tcheeric.nostr.event.marshaller.BaseMarshaller;

/**
 *
 * @author squirrel
 */
public class FiltersMarshaller extends BaseMarshaller {

    public FiltersMarshaller(BaseMessage message, Relay relay) {
        super(message, relay);
    }

    
    @Override
    public String marshall() throws NostrException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
