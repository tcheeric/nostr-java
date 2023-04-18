package nostr.event.marshaller.impl;

import java.util.List;

import nostr.base.ITag;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;
import nostr.event.marshaller.BaseElementMarshaller;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

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
        ITag tag = (ITag) getElement();
        Relay relay = getRelay();

        if (!nipSupportForTag()) {
            throw new UnsupportedNIPException(relay + " does not support tag " + tag.getCode());
        }
        
        return toJson(tag);
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

        if (tag instanceof GenericTag genericTag) {
            nip = genericTag.getNip();
            return snips.contains(nip);
        } else {
            return NipUtil.checkSupport(relay, tag) && NipUtil.checkSupport(relay, ((BaseTag) tag).getParent());
        }

    }
}
