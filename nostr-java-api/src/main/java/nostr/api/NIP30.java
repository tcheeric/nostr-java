/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.event.tag.EmojiTag;

public class NIP30 {
    
    /**
     * 
     * @param shortcode
     * @param imageUrl
     * @return 
     */
    public static EmojiTag createCustomEmojiTag(@NonNull String shortcode, @NonNull String  imageUrl) {
        return new EmojiTag(shortcode, imageUrl);
    }    
}
