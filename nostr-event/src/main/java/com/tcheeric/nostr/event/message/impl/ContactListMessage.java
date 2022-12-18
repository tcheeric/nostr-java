package com.tcheeric.nostr.event.message.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.impl.GenericEvent;
import com.tcheeric.nostr.event.list.PubKeyTagList;
import com.tcheeric.nostr.event.message.EventMessage;
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
