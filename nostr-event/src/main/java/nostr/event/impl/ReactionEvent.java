package nostr.event.impl;

import nostr.base.NostrException;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.Reaction;
import nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.NIPSupport;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@NIPSupport(value=25, description = "Reactions")
public class ReactionEvent extends GenericEvent {

    private final GenericEvent sourceEvent;

    public ReactionEvent(PublicKey pubKey, TagList tags, Reaction content, GenericEvent sourceEvent) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, Kind.REACTION, tags, content.getEmoji());
        this.sourceEvent = sourceEvent;
    }

}
