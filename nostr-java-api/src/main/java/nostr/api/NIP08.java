/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP08.MentionsEventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.MentionsEvent;

/**
 *
 * @author eric
 */
public class NIP08 {
    
    /**
     * 
     * @param content
     * @return 
     */
    public static MentionsEvent createMentionsEvent(String content) {
        return new MentionsEventFactory(content).create();
    }

    /**
     * 
     * @param tags
     * @param content
     * @return 
     */
    public static MentionsEvent createMentionsEvent(List<BaseTag> tags, String content) {
        return new MentionsEventFactory(tags, content).create();
    }
}
