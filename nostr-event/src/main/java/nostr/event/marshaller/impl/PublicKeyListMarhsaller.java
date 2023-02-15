
package nostr.event.marshaller.impl;

import nostr.base.Relay;
import nostr.event.list.PublicKeyList;
import nostr.event.marshaller.BaseListMarhsaller;

/**
 *
 * @author squirrel
 */
public class PublicKeyListMarhsaller extends BaseListMarhsaller {

    public PublicKeyListMarhsaller(PublicKeyList publicKeyList, Relay relay) {
        super(publicKeyList, relay);
    }
    
}
