package nostr.event.message;

import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.list.PubKeyTagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class ContactListMessage extends EventMessage {
        
    public ContactListMessage(PubKeyTagList contactList, PublicKey publicKey) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {        
        super(new GenericEvent(publicKey, Kind.CONTACT_LIST, contactList));        
    }
    
}
