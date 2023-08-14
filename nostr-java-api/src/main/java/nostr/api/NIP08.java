/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP08.MentionsEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.MentionsEvent;

/**
 *
 * @author eric
 */
public class NIP08 {
    
    /**
     * Create a NIP08 mentions event without pubkey tags
     * @param content the note's content 
     * @return the mentions event without pubkey tags
     */
    public static MentionsEvent createMentionsEvent(@NonNull String content) {
        return new MentionsEventFactory(content).create();
    }

    /**
     * Create a NIP08 mentions event 
     * @param publicKeys the referenced public keys
     * @param content the note's content containing the references to the public keys
     * @return the mentions event
     */
    public static MentionsEvent createMentionsEvent(@NonNull List<PublicKey> publicKeys, @NonNull String content) {
        return new MentionsEventFactory(publicKeys, content).create();
    }
}
