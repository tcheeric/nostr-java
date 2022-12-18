
package com.tcheeric.nostr.event.marshaller.impl;

import com.tcheeric.nostr.base.Relay;
import com.tcheeric.nostr.event.list.PublicKeyList;
import com.tcheeric.nostr.event.marshaller.BaseListMarhsaller;

/**
 *
 * @author squirrel
 */
public class PublicKeyListMarhsaller extends BaseListMarhsaller {

    public PublicKeyListMarhsaller(PublicKeyList publicKeyList, Relay relay) {
        super(publicKeyList, relay);
    }
    
}
