/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.tag.GenericTag;

public class NIP30 {
    
    /**
     * 
     * @param shortcode
     * @param imageUrl
     */
    public static GenericTag createEmojiTag(@NonNull String shortcode, @NonNull String imageUrl) {
        return new GenericTagFactory(Constants.Tag.EMOJI_CODE, shortcode, imageUrl).create();
    }
}
