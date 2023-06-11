
package nostr.event.impl;

import java.util.List;
import java.util.Map;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Event(name = "OpenTimestamps Attestations for Events", nip = 1)
public class OtsEvent extends TextNoteEvent {
    
    public OtsEvent(PublicKey pubKey, List<? extends BaseTag> tags, String content, String ots) {
        super(pubKey, tags, content);
        var attribute = ElementAttribute.builder().nip(3).value(Map.of("ots", ots)).build();
        this.addAttribute(attribute);
    }
        
}
