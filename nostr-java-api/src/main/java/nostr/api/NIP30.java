/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import nostr.api.factory.impl.NIP30.CustomEmojiTagFactory;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
public class NIP30 extends Nostr {
    
    public static GenericTag createCustomEmojiTag(String emoji, URL url) {
        return new CustomEmojiTagFactory(emoji, url).create();
    }    
}
