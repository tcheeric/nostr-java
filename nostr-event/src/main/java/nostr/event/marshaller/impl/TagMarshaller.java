package nostr.event.marshaller.impl;

import nostr.base.ITag;
import nostr.base.Relay;
import nostr.util.UnsupportedNIPException;
import nostr.event.BaseTag;
import nostr.event.marshaller.BaseElementMarshaller;
import java.util.List;
import static nostr.base.NipUtil.checkSupport;
import nostr.event.impl.GenericTag;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public class TagMarshaller extends BaseElementMarshaller {

    public TagMarshaller(ITag tag, Relay relay) {
        this(tag, relay, false);
    }

    public TagMarshaller(ITag iTag, Relay relay, boolean escape) {
        super(iTag, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }

    private String toJson() throws NostrException {

        ITag tag = (ITag) getElement();
        Relay relay = getRelay();

        if (!nipSupportForTag()) {
            throw new UnsupportedNIPException(relay + " does not support tag " + tag.getCode());
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        if (!isEscape()) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }

        result.append(tag.getCode());

        if (!isEscape()) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }

        result.append(tag.printAttributes(relay, isEscape()));
        result.append("]");

        return result.toString();
    }

    // TODO test me
    private boolean nipSupportForTag() {

        Relay relay = getRelay();
        if (relay == null) {
            return true;
        }
        
        List<Integer> snips = relay.getSupportedNips();
        Integer nip;

        ITag tag = (ITag) getElement();
        if (tag == null) {
            return false;
        }

        if (tag instanceof GenericTag) {
            nip = ((GenericTag) tag).getNip();
            return snips.contains(nip);
        } else {
            return checkSupport(relay, tag) && checkSupport(relay, ((BaseTag) tag).getParent());
        }

    }
}
