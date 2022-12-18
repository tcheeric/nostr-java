package nostr.event.message.impl;

import nostr.base.NostrException;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.list.PubKeyTagList;
import nostr.event.message.EventMessage;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author squirrel
 */
public class ContactListMessage extends EventMessage {
        
    public ContactListMessage(PubKeyTagList contactList, PublicKey publicKey) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {        
        super(new GenericEvent(publicKey, Kind.CONTACT_LIST, contactList));        
    }
    
}
