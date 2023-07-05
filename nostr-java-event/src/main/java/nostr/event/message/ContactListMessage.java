package nostr.event.message;

import java.util.List;
import lombok.ToString;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
@ToString
public class ContactListMessage extends EventMessage {
        
    public ContactListMessage(List<BaseTag> contactList, PublicKey publicKey) {        
        super(new GenericEvent(publicKey, Kind.CONTACT_LIST, contactList));        
    }
    
}
