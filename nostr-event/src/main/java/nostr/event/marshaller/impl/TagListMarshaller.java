package nostr.event.marshaller.impl;

import static nostr.base.NipUtil.checkSupport;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

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

        StringBuilder result = new StringBuilder();
        Relay relay = getRelay();

        final List<ITag> list = getSupportedTags(relay).getList();
        result.append("[");
        if (!list.isEmpty()) {
            result.append(list.stream().filter(t -> t != null).map(t -> {
                try {
                    return new TagMarshaller(t, relay, isEscape()).marshall();
                } catch (NostrException ex) {
                    log.log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }).collect(Collectors.joining(",")));            
        }
        result.append("]");

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
