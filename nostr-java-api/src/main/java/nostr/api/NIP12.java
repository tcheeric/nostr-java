/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

import java.net.URL;
import java.util.List;

/**
 *
 * @author eric
 */
public class NIP12 {

    /**
     * Create a hashtag tag
     * @param hashtag the hashtag
     */
    public static BaseTag createHashtagTag(@NonNull String hashtag) {
        return new BaseTagFactory(Constants.Tag.HASHTAG_CODE, List.of(hashtag)).create();
    }

    /**
     * Create an URL tag
     * @param url the reference
     */
    public static BaseTag createReferenceTag(@NonNull URL url) {
        return new BaseTagFactory(Constants.Tag.REFERENCE_CODE, List.of(url.toString())).create();
    }

    /**
     * Create a Geo tag 
     * @param location the geohash
     */
    public static BaseTag createGeohashTag(@NonNull String location) {
        return new BaseTagFactory(Constants.Tag.GEOHASH_CODE, List.of(location)).create();
    }
}
