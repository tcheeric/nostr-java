package nostr.event.message;

import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.base.list.PubKeyTagList;
import nostr.event.impl.GenericEvent;

/**
 *
 * @author squirrel
 */
public class ContactListMessage extends EventMessage {
        
    public ContactListMessage(PubKeyTagList contactList, PublicKey publicKey) {        
        super(new GenericEvent(publicKey, Kind.CONTACT_LIST, contactList));        
    }
    
}
