package nostr.event.unmarshaller.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.GenericEvent;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
public class EventUnmarshaller extends BaseElementUnmarshaller {

    public EventUnmarshaller(String event) {
        this(event, false);
    }

    public EventUnmarshaller(String event, boolean escape) {
        super(event, escape);
    }

    @Override
    public IEvent unmarshall() throws NostrException {
        var value = new JsonObjectUnmarshaller(this.getJson()).unmarshall();

        // Public Key
        var strPubKey = value.get("\"pubkey\"").get().getValue().toString();
        var pubKey = new PublicKey(NostrUtil.hexToBytes(strPubKey));

        // Kind 
        var ikind = ((Number) value.get("\"kind\"").get().getValue()).intValue();
        Kind kind = Kind.valueOf(ikind);

        // TagList
        var strTagList = value.get("\"tags\"").get().toString();
        var tags = new TagListUnmarshaller(strTagList, isEscape()).unmarshall();

        // Content 
        var content = value.get("\"content\"").get().getValue().toString();

        try {
            return new GenericEvent(pubKey, kind, tags, content);
        } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            throw new NostrException(ex);
        }
    }

}
