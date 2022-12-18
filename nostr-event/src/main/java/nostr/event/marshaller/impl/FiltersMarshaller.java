
package nostr.event.marshaller.impl;

import nostr.base.NostrException;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.marshaller.BaseMarshaller;

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
