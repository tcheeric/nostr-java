
package nostr.event.marshaller.impl;

import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.event.list.TagList;
import java.util.List;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class TagListMarshaller extends BaseListMarhsaller {
    
    public TagListMarshaller(TagList list, Relay relay) {
        this(list, relay, false);
    }

    public TagListMarshaller(TagList tagList, Relay relay, boolean escape) {
        super(tagList, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        
        StringBuilder result = new StringBuilder();
        TagList tagList = (TagList) getList();
        Relay relay = getRelay();
        
        result.append("[");
        @SuppressWarnings("rawtypes") final List list = tagList.getList();
        if (!list.isEmpty()) {
            int size = tagList.size(), i = 0;

            for (Object t : list) {
                if (t == null) {
                    continue;
                }
                ITag tag = (ITag) t;
                result.append( new TagMarshaller(tag, relay, isEscape()).marshall());
                if (++i < size) {
                    result.append(",");
                }
            }
        }
        result.append("]");
        
        return result.toString();
    }    
}
