/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import java.util.List;
import nostr.api.factory.impl.NIP25.ReactionEventFactory;
import nostr.event.BaseTag;
import nostr.event.Reaction;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ReactionEvent;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP25 extends Nostr {

    /**
     * Create a Reaction event
     * @param event the related event to react to
     * @param reaction
     * @return 
     */
    public static ReactionEvent createReactionEvent(GenericEvent event, Reaction reaction) {
        return new ReactionEventFactory(event, reaction).create();
    }
    
    /**
     * Create a NIP25 Reaction event to react to a specific event
     * @param event the related event to react to
     * @param content MAY be an emoji, or NIP-30 custom emoji
     * @param emoji the emoji image url
     * @return 
     */
    public static ReactionEvent createReactionEvent(GenericEvent event, String content, URL emoji) {
        return new ReactionEventFactory(event, content, emoji).create();
    }

    /**
     * Create a NIP25 Reaction event to react to several event and/or pubkeys
     * @param tags the list of events or pubkeys to react to
     * @param content MAY be an emoji, or NIP-30 custom emoji
     * @param emoji the emoji image url
     * @return 
     */
    public static ReactionEvent createReactionEvent(List<BaseTag> tags, String content, URL emoji) {
        return new ReactionEventFactory(tags, content, emoji).create();
    }

    /**
     * 
     * @param event
     * @throws NostrException 
     */
    public static void like(GenericEvent event) throws NostrException {
        react(event, Reaction.LIKE.getEmoji(), null);
    }

    /**
     * 
     * @param event
     * @throws NostrException 
     */
    public static void dislike(GenericEvent event) throws NostrException {
        react(event, Reaction.DISLIKE.getEmoji(), null);
    }
    
    /**
     * 
     * @param event
     * @param reaction
     * @param url
     * @throws NostrException 
     */
    public static void react(GenericEvent event, String reaction, URL url) throws NostrException {
        var reactionEvent = new ReactionEventFactory(event, reaction, url).create();

        Nostr.sign(reactionEvent);
        Nostr.send(reactionEvent);        
    }
}
