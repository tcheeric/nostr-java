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

    public static ReactionEvent createReactionEvent(GenericEvent event, Reaction reaction) {
        return new ReactionEventFactory(event, reaction).create();
    }
    
    public static ReactionEvent createReactionEvent(GenericEvent event, Reaction reaction, URL emoji) {
        return new ReactionEventFactory(event, reaction.getEmoji(), emoji).create();
    }

    public static ReactionEvent createReactionEvent(List<BaseTag> tags, GenericEvent event, Reaction reaction, URL emoji) {
        return new ReactionEventFactory(tags, event, reaction.getEmoji(), emoji).create();
    }

    public static void like(GenericEvent event) throws NostrException {
        react(event, Reaction.LIKE.getEmoji(), null);
    }

    public static void dislike(GenericEvent event) throws NostrException {
        react(event, Reaction.DISLIKE.getEmoji(), null);
    }
    
    public static void react(GenericEvent event, String reaction, URL url) throws NostrException {
        var reactionEvent = new ReactionEventFactory(event, reaction, url).create();

        Nostr.sign(reactionEvent);
        Nostr.send(reactionEvent);        
    }
}
