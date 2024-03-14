/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.AbstractTagFactory;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.ReferenceTag;

import java.net.URL;

/**
 *
 * @author eric
 */
public class NIP12Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class HashtagTagFactory extends AbstractTagFactory<HashtagTag> {

        private final String hashtag;

        public HashtagTagFactory(@NonNull String hashtag) {
            this.hashtag = hashtag;
        }

        @Override
        public HashtagTag create() {
            return new HashtagTag(hashtag);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ReferenceTagFactory extends AbstractTagFactory<ReferenceTag> {

        private final URL url;

        public ReferenceTagFactory(@NonNull URL url) {
            this.url = url;
        }

        @Override
        public ReferenceTag create() {
            return new ReferenceTag(url);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class GeohashTagFactory extends AbstractTagFactory<GeohashTag> {

        private final String location;

        public GeohashTagFactory(@NonNull String location) {
            this.location = location;
        }

        @Override
        public GeohashTag create() {
            return new GeohashTag(location);
        }
    }

}
