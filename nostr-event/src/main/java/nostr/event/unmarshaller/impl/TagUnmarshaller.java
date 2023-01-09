
package nostr.event.unmarshaller.impl;

import java.util.HashSet;
import java.util.Set;
import nostr.base.ElementAttribute;
import nostr.base.ITag;
import nostr.event.impl.GenericTag;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;

/**
 *
 * @author squirrel
 */
public class TagUnmarshaller extends BaseElementUnmarshaller {
    
    public TagUnmarshaller(String tag) {
        this(tag, false);
    }

    public TagUnmarshaller(String tag, boolean escape) {
        super(tag, escape);
    }

    @Override
    public ITag unmarshall() {
        var value = new JsonArrayUnmarshaller(this.getJson()).unmarshall();
        
        String code = value.get(0).get().getValue().toString();
        Set<ElementAttribute> tagAttrs = new HashSet<>();
        
        for(int i = 1; i< value.length(); i++) {
            ElementAttribute attr = ElementAttribute.builder().value(value.get(i).get()).build();
            tagAttrs.add(attr);
        }
        
        return new GenericTag(1, code, tagAttrs);
    }
    
}
