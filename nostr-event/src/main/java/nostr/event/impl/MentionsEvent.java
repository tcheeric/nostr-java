
package nostr.event.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import nostr.event.tag.PubKeyTag;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.base.list.PubKeyTagList;
import nostr.base.list.TagList;
import java.util.List;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Handling Mentions", nip = 8)
@Log
public final class MentionsEvent extends GenericEvent {

    public final PubKeyTagList mentionees;

    public MentionsEvent(PublicKey pubKey, TagList tags, String content, PubKeyTagList mentionees) {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
        this.mentionees = mentionees;       
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() throws NostrException {
        try {
            super.update();
        } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }

        this.getTags().addAll(mentionees);

        int index = 0;
        final List<PubKeyTag> pkTagList = mentionees.getList();

        for (PubKeyTag t : pkTagList) {
            String replacement = "#[" + index++ + "]";
            setContent(this.getContent().replace(t.getPublicKey().toString(), replacement));
        }
    }
}
