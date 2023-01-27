package nostr.event.marshaller.impl;

import nostr.base.ITag;
import nostr.base.Relay;
import nostr.event.marshaller.BaseListMarhsaller;
import nostr.base.list.TagList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import static nostr.base.NipUtil.checkSupport;
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

        StringBuilder result = new StringBuilder();
        TagList tagList = (TagList) getList();
        Relay relay = getRelay();

        final List<ITag> list = getSupportedTags(relay).getList();
        if (!list.isEmpty()) {
            result.append("[");

            result.append(list.stream().filter(t -> t != null).map(t -> {
                try {
                    return new TagMarshaller(t, relay, isEscape()).marshall();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.joining(",")));
            
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
            if (relay == null || checkSupport(relay, (ITag) t)) {
                result.add(t);
            }
        }
        return result;
    }
}
