
package nostr.event.impl;

import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.StringValue;

/**
 *
 * @author squirrel
 */
@Event(name = "OpenTimestamps Attestations for Events", nip = 1)
public class OtsEvent extends TextNoteEvent {
    
    public OtsEvent(PublicKey pubKey, TagList tags, String content, String ots) {
        super(pubKey, tags, content);
        var attribute = ElementAttribute.builder().nip(3).value(new ExpressionValue("ots", new StringValue(ots))).build();
        this.addAttribute(attribute);
    }
        
}
