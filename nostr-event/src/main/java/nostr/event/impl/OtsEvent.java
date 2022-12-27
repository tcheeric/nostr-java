
package nostr.event.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.event.list.TagList;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class OtsEvent extends TextNoteEvent {
    
    public OtsEvent(PublicKey pubKey, TagList tags, String content, String ots) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, tags, content);
        List<String> valueList = new ArrayList<>();
        valueList.add(ots);
        this.addAttribute(ElementAttribute.builder().name("ots").valueList(valueList).nip(3).build());
    }
        
}
