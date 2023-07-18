/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Data;
import nostr.base.IEvent;
import nostr.base.PublicKey;

/**
 *
 * @author eric
 * @param <T>
 */
@Data
public abstract class EventFactory<T extends IEvent> {

    private final PublicKey sender;
    private final String content;

    public EventFactory(PublicKey sender, String content) {
        this.content = content;
        this.sender = sender;
    }

    public abstract T create();
}
