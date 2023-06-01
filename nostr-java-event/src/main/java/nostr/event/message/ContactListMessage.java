package nostr.event.message;

import lombok.ToString;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.list.PubKeyTagList;

/**
 *
 * @author squirrel
 */
@ToString
public class ContactListMessage extends EventMessage {
        
    public ContactListMessage(PubKeyTagList contactList, PublicKey publicKey) {        
        super(new GenericEvent(publicKey, Kind.CONTACT_LIST, contactList));        
    }
    
}
