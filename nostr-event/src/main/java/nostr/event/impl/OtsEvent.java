
package nostr.event.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.event.list.TagList;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class OtsEvent extends TextNoteEvent {
    
    public OtsEvent(PublicKey pubKey, TagList tags, String content, String ots) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, tags, content);
        var attribute = ElementAttribute.builder().name("ots").nip(3).value(new ExpressionValue("ots", new StringValue(ots))).build();
        this.addAttribute(attribute);
    }
        
}
