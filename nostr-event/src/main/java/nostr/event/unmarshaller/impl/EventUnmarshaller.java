package nostr.event.unmarshaller.impl;

import java.util.logging.Level;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@Log
public class EventUnmarshaller extends BaseElementUnmarshaller<GenericEvent> {

    public EventUnmarshaller(String event) {
        this(event, false);
    }

    public EventUnmarshaller(String event, boolean escape) {
        super(event, escape);
    }

    @Override
    public GenericEvent unmarshall() {
        var value = new JsonObjectUnmarshaller(this.getJson()).unmarshall();

        // Public Key
        var strPubKey = value.get("pubkey").get().getValue().toString();
        var pubKey = new PublicKey(NostrUtil.hexToBytes(strPubKey));

        // Kind 
        var ikind = ((Number) value.get("kind").get().getValue()).intValue();
        Kind kind = Kind.valueOf(ikind);

        // TagList
        var strTagList = value.get("tags").get().toString();
        var tags = new TagListUnmarshaller(strTagList, isEscape()).unmarshall();

        // Content 
        var content = value.get("content").get().getValue().toString();

        // Created At
        var createdAt = value.get("created_at").get().getValue();

        // Event Id
        var id = value.get("id").get().getValue().toString();

        // Signature
        var sig = value.get("sig").get().getValue().toString();

        var event = new GenericEvent(pubKey, kind, tags, content);

        event.setCreatedAt(Math.round((Double) createdAt));
        event.setId(id);
        event.setSignature(new Signature(NostrUtil.hexToBytes(sig), pubKey));

        return event;
    }

}
