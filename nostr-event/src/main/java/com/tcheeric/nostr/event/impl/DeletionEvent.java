
package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.tcheeric.nostr.base.annotation.NIPSupport;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NIPSupport(value=9, description="Event Deletion")
public class DeletionEvent extends GenericEvent {

    public DeletionEvent(PublicKey pubKey, TagList tagList, String content) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        
        super(pubKey, Kind.DELETION, tagList, content);        
    }

    public DeletionEvent(PublicKey pubKey, TagList tagList) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        
        this(pubKey, tagList, "Deletion request");
    }
}
