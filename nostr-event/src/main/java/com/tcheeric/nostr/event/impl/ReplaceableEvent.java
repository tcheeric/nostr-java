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
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.NIPSupport;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@NIPSupport(value = 16, description = "Replaceable Events")
public class ReplaceableEvent extends GenericEvent {

    private final GenericEvent original;

    public ReplaceableEvent(PublicKey pubKey, TagList tags, String content, GenericEvent original) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        super(pubKey, Kind.DELETION, tags, content);
        this.original = original;
    }

}
