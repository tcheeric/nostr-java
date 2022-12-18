
package nostr.event.impl;

import nostr.event.tag.PubKeyTag;
import nostr.base.NostrException;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.list.PubKeyTagList;
import nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.tcheeric.nostr.base.annotation.NIPSupport;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NIPSupport(value=8, description = "Handling Mentions")
public final class MentionsEvent extends GenericEvent {

    public final PubKeyTagList mentionees;

    public MentionsEvent(PublicKey pubKey, TagList tags, String content, PubKeyTagList mentionees) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
        this.mentionees = mentionees;       
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super.update();

        this.getTags().addAll(mentionees);

        int index = 0;
        final List<PubKeyTag> pkTagList = mentionees.getList();

        for (PubKeyTag t : pkTagList) {
            String replacement = "#[" + index++ + "]";
            setContent(this.getContent().replace(t.getPublicKey().toString(), replacement));
        }
    }
}
