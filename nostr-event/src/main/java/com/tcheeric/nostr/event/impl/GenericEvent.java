/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.ISignable;
import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.NostrUtil;
import com.tcheeric.nostr.base.annotation.JsonString;
import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.Signature;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.base.UnsupportedNIPException;
import com.tcheeric.nostr.event.BaseEvent;
import com.tcheeric.nostr.event.list.TagList;
import com.tcheeric.nostr.event.Kind;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import lombok.Data;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.event.marshaller.impl.EventMarshaller;
import com.tcheeric.nostr.event.marshaller.impl.TagListMarshaller;
import java.beans.Transient;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@NIPSupport
public class GenericEvent extends BaseEvent implements ISignable {

    @Key
    @EqualsAndHashCode.Include
    private String id;

    @Key(name = "pubkey")
    @EqualsAndHashCode.Include
    @JsonString
    private PublicKey pubKey;

    @Key(name = "created_at")
    @EqualsAndHashCode.Exclude
    private Long createdAt;

    @Key
    @EqualsAndHashCode.Exclude
    private Kind kind;

    @Key
    @EqualsAndHashCode.Exclude
    private TagList tags;

    @Key
    @EqualsAndHashCode.Exclude
    private String content;

    @Key(name = "sig")
    @EqualsAndHashCode.Exclude
    @JsonString
    private Signature signature;

    @Key
    @EqualsAndHashCode.Exclude
    @NIPSupport(3)
    private String ots;

    @EqualsAndHashCode.Exclude
    private byte[] _serializedEvent;

    public GenericEvent(@NonNull PublicKey pubKey, @NonNull Kind kind, @NonNull TagList tags) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this(pubKey, kind, tags, null, null);
    }

    public GenericEvent(PublicKey pubKey, Kind kind, TagList tags, String content) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this(pubKey, kind, tags, content, null);
    }

    public GenericEvent(PublicKey pubKey, Kind kind, TagList tags, String content, String ots) throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this.pubKey = pubKey;
        this.kind = kind;
        this.tags = tags;
        this.content = content;
        this.ots = ots;
    }

    @Override
    public String toString() {
        try {
            return new EventMarshaller(this, null).marshall();
        } catch (UnsupportedNIPException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public void setTags(TagList tags) {

        @SuppressWarnings("rawtypes")
        List list = tags.getList();

        for (Object o : list) {
            ((ITag) o).setParent(this);
        }
    }

    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        this.createdAt = Instant.now().getEpochSecond();

        this._serializedEvent = this.serialize().getBytes(StandardCharsets.UTF_8);

        this.id = NostrUtil.bytesToHex(NostrUtil.sha256(_serializedEvent));
    }

    @Transient
    public boolean isSigned() {
        return this.signature != null;
    }

    @SuppressWarnings("unchecked")
    private String serialize() throws NostrException {
        var sb = new StringBuilder();
        sb.append("[");
        sb.append("0").append(",\"");
        sb.append(this.pubKey).append("\",");
        sb.append(this.createdAt).append(",");
        sb.append(this.kind.getValue()).append(",");

        sb.append(new TagListMarshaller(tags, null).marshall());
        sb.append(",\"");
        sb.append(this.content);
        sb.append("\"]");

        return sb.toString();
    }
}
