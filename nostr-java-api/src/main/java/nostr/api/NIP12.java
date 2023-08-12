/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import nostr.api.factory.impl.NIP12.GeohashTagFactory;
import nostr.api.factory.impl.NIP12.HashtagTagFactory;
import nostr.api.factory.impl.NIP12.ReferenceTagFactory;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.ReferenceTag;

/**
 *
 * @author eric
 */
public class NIP12 extends Nostr {

    public static HashtagTag createHashtagTag(String hashtag) {
        return new HashtagTagFactory(hashtag).create();
    }
    
    public static ReferenceTag createReferenceTag(URL url) {
        return new ReferenceTagFactory(url).create();
    }
    
    public static GeohashTag createGeohashTag(String location) {
        return new GeohashTagFactory(location).create();
    }    
}
