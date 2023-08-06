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
import nostr.id.Identity;

/**
 *
 * @author eric
 * @param <T>
 */
@Data
public abstract class EventFactory<T extends IEvent> {

    private final PublicKey sender;
    private final String content;
    private final List<BaseTag> tags;
    
    public EventFactory(String content) {
        this.content = content;
        this.tags = new ArrayList<>();
        this.sender = getSenderPublicKey();
    }

    public EventFactory(List<BaseTag> tags, String content) {
        this.content = content;
        this.tags = tags;
        this.sender = getSenderPublicKey();
    }

    public EventFactory(PublicKey sender, String content) {
        this(sender, new ArrayList<>(), content);
    }

    public EventFactory(PublicKey sender, List<BaseTag> tags, String content) {
        this.content = content;
        this.tags = tags;
        this.sender = sender;
    }

    public abstract T create();

    private PublicKey getSenderPublicKey() {
        Identity identity = Identity.getInstance();
        return identity.getPublicKey();
    }
}
