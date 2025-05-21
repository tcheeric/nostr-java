/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import lombok.Data;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
@Data
public abstract class EventFactory {

    private final Identity identity;
    private final String content;
    private final List<BaseTag> tags;

    public EventFactory(Identity identity) {
        this(identity, new ArrayList<>(), "");
    }

    protected EventFactory() {
        this.identity = null;
        this.content = "";
        this.tags = new ArrayList<>();
    }

    public EventFactory(Identity sender, String content) {
        this(sender, new ArrayList<>(), content);
    }

    public EventFactory(Identity sender, List<BaseTag> tags, String content) {
        this.content = content;
        this.tags = tags;
        this.identity = sender;
    }

    public abstract GenericEvent create();
    
    protected void addTag(BaseTag tag) {
        this.tags.add(tag);
    }

    protected PublicKey getSender() {
        if (this.identity != null) {
            return this.identity.getPublicKey();
        }
        return null;
    }
}
