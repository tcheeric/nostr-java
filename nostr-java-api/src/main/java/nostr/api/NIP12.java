/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP12Impl.GeohashTagFactory;
import nostr.api.factory.impl.NIP12Impl.HashtagTagFactory;
import nostr.api.factory.impl.NIP12Impl.ReferenceTagFactory;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.ReferenceTag;

import java.net.URI;

/**
 *
 * @author eric
 */
public class NIP12 {

    /**
     * Create a hashtag tag
     * @param hashtag the hashtag
     */
    public static HashtagTag createHashtagTag(@NonNull String hashtag) {
        return new HashtagTagFactory(hashtag).create();
    }

    /**
     * Create an URL tag
     * @param url the reference
     */
    public static ReferenceTag createReferenceTag(@NonNull URI url) {
        return new ReferenceTagFactory(url).create();
    }

    /**
     * Create a Geo tag 
     * @param location the geohash
     */
    public static GeohashTag createGeohashTag(@NonNull String location) {
        return new GeohashTagFactory(location).create();
    }
}
