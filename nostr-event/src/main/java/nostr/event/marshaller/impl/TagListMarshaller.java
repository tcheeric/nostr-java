package nostr.event.marshaller.impl;

import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.event.list.TagList;
import java.util.List;
import static nostr.base.NipUtil.checkSupport;
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

        @SuppressWarnings("rawtypes")
        final List list = getSupportedTags(relay).getList();
        if (!list.isEmpty()) {
            result.append("[");
            int size = tagList.size(), i = 0;

            for (Object t : list) {
                if (t == null) {
                    continue;
                }
                ITag tag = (ITag) t;
                result.append(new TagMarshaller(tag, relay, isEscape()).marshall());
                if (++i < size) {
                    result.append(",");
                }
            }
            result.append("]");
        }

        return result.toString();
    }

    private TagList getSupportedTags(Relay relay) {
        TagList tagList = (TagList) getList();
        TagList result = new TagList();

        for (var t : tagList.getList()) {
            if (t == null) {
                continue;
            }
            if (relay == null || checkSupport(relay, (ITag) t)/*relay.getSupportedNips().contains(getNip((ITag) t))*/) {
                result.add(t);
            }
        }
        return result;
    }
}
