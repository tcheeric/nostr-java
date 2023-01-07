/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.base.IEvent;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Log
public abstract class BaseEvent implements IEvent {

    public static class ProxyEvent extends GenericEvent {

        public ProxyEvent(String id) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
            super(new PublicKey(new byte[]{}), Kind.UNDEFINED);
            setId(id);
        }

    }
}
