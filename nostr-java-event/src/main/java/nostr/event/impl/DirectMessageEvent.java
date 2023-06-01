package nostr.event.impl;

import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author squirrel
 */
@Event(name = "Encrypted Direct Message", nip = 4)
public class DirectMessageEvent extends GenericEvent {

    public DirectMessageEvent(PublicKey sender, TagList tags, String content) {
        super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE, tags, content);
    }
    
    public DirectMessageEvent(PublicKey sender, PublicKey recipient, String content) {
        super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE);
        this.setContent(content);        
        this.addTag(PubKeyTag.builder().publicKey(recipient).build());        
    }
}
