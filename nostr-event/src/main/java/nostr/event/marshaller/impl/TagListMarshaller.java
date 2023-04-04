package nostr.event.marshaller.impl;

import static nostr.base.NipUtil.checkSupport;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.java.Log;
import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.list.TagList;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Log
public class TagListMarshaller extends BaseListMarhsaller {

    public TagListMarshaller(TagList list, Relay relay) {
        this(list, relay, false);
    }

    public TagListMarshaller(TagList tagList, Relay relay, boolean escape) {
        super(tagList, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        Relay relay = getRelay();

        final List<ITag> list = getSupportedTags(relay).getList();
        
        try {
			return getMapper().writeValueAsString(list);
		} catch (JsonProcessingException e) {
			throw new NostrException(e);
		}
    }

    private TagList getSupportedTags(Relay relay) {
        TagList tagList = (TagList) getElement();
        TagList result = new TagList();

        for (var t : tagList.getList()) {
            if (t == null) {
                continue;
            }
            if (relay == null || checkSupport(relay, (ITag) t)) {
                result.add(t);
            }
        }
        return result;
    }
}
