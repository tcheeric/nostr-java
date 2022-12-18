
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
@NIPSupport(value=4, description = "Encrypted Direct Message")
public class DirectMessageEvent extends GenericEvent {

    public DirectMessageEvent(PublicKey sender, TagList tags, String content) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(sender, Kind.ENCRYPTED_DIRECT_MESSAGE, tags, content);
    }
}
