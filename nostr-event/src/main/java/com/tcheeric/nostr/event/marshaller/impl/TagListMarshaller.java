
package com.tcheeric.nostr.event.marshaller.impl;

import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Relay;
import com.tcheeric.nostr.event.marshaller.BaseListMarhsaller;
import com.tcheeric.nostr.event.list.TagList;
import java.util.List;

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
