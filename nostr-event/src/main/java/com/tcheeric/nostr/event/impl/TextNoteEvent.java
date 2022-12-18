
package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author squirrel
 */
@NIPSupport(value = 1, description = "Basic Event Kinds: text_note")
public class TextNoteEvent extends GenericEvent {

    public TextNoteEvent(PublicKey pubKey, TagList tags, String content) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, Kind.TEXT_NOTE, tags, content);
    }   
}
