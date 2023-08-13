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

    /**
     * Create a hashtag tag
     * @param hashtag the hashtag
     * @return 
     */
    public static HashtagTag createHashtagTag(String hashtag) {
        return new HashtagTagFactory(hashtag).create();
    }
    
    /**
     * Create an URL tag
     * @param url the reference
     * @return 
     */
    public static ReferenceTag createReferenceTag(URL url) {
        return new ReferenceTagFactory(url).create();
    }
    
    /**
     * Create a Geo tag 
     * @param location the geohash
     * @return 
     */
    public static GeohashTag createGeohashTag(String location) {
        return new GeohashTagFactory(location).create();
    }    
}
