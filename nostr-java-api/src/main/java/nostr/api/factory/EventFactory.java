/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.id.IIdentity;
import nostr.id.Identity;

/**
 *
 * @author eric
 * @param <T>
 */
@Data
public abstract class EventFactory<T extends IEvent> {

    private final IIdentity identity;
    private final String content;
    private final List<BaseTag> tags;

    public EventFactory(IIdentity identity) {
        this(identity, new ArrayList<>(), null);
    }

    protected EventFactory() {
        this.identity = Identity.getInstance();
        this.content = null;
        this.tags = new ArrayList<>();
    }

    public EventFactory(String content) {
        this.content = content;
        this.tags = new ArrayList<>();
        this.identity = Identity.getInstance();
    }

    public EventFactory(List<BaseTag> tags, String content) {
        this.content = content;
        this.tags = tags;
        this.identity = Identity.getInstance();
    }

    public EventFactory(IIdentity sender, String content) {
        this(sender, new ArrayList<>(), content);
    }

    public EventFactory(IIdentity sender, List<BaseTag> tags, String content) {
        this.content = content;
        this.tags = tags;
        this.identity = sender;
    }

    public abstract T create();
    
    protected void addTag(BaseTag tag) {
        this.tags.add(tag);
    }

    protected PublicKey getSender() {
        return this.identity.getPublicKey();
    }
}
