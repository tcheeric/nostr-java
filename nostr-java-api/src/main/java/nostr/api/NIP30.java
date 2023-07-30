/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.base.ElementAttribute;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
public class NIP30 extends Api {
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class CustomEmojiTagFactory extends TagFactory<GenericTag> {

        private final String emoji;
        private final URL url; 
        
        public CustomEmojiTagFactory(@NonNull String emoji, @NonNull URL url) {
            this.emoji = emoji;
            this.url = url;
        }
        
        @Override
        public GenericTag create() {
            
            // Create the tag attributes
            Set<ElementAttribute> attributes = new HashSet<>();
            ElementAttribute attrName = new ElementAttribute("reaction", getEmoji(), 30);
            ElementAttribute attrUrl = new ElementAttribute("url", getUrl().toString(), 30);
            attributes.add(attrUrl);
            attributes.add(attrName);
            
            // Create the emoji tag
            return new GenericTag("emoji", 30, attributes);
        }        
    }    
}
