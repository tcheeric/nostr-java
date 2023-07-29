/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.api.factory.TagFactory;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
public class NIP23 extends Api {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class LongFormContentEventFactory extends EventFactory<GenericEvent> {

        public LongFormContentEventFactory(List<BaseTag> tags, String content) {
            super(content);
        }
        
        @Override
        public GenericEvent create() {
            return new GenericEvent(getSender(), Kinds.KIND_PRE_LONG_FORM_CONTENT, getTags(), getContent());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class TitleTagFactory extends TagFactory<GenericTag> {

        private final String title;

        @Override
        public GenericTag create() {
            final var attr = ElementAttribute.builder().nip(23).name("title").value(title).build();
            final Set<ElementAttribute> attributes = new HashSet<>();
            attributes.add(attr);
            return new GenericTag("title", 23, attributes);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class ImageTagFactory extends TagFactory<GenericTag> {

        private final URL url;

        @Override
        public GenericTag create() {
            final var attr = ElementAttribute.builder().nip(23).name("url").value(url.toString()).build();
            final Set<ElementAttribute> attributes = new HashSet<>();
            attributes.add(attr);
            return new GenericTag("url", 23, attributes);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class SummaryTagFactory extends TagFactory<GenericTag> {

        private final String summary;

        @Override
        public GenericTag create() {
            final var attr = ElementAttribute.builder().nip(23).name("summary").value(summary).build();
            final Set<ElementAttribute> attributes = new HashSet<>();
            attributes.add(attr);
            return new GenericTag("summary", 23, attributes);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class PublishedAtTagFactory extends TagFactory<GenericTag> {

        private final Integer date;

        @Override
        public GenericTag create() {
            final var attr = ElementAttribute.builder().nip(23).name("date").value(date.toString()).build();
            final Set<ElementAttribute> attributes = new HashSet<>();
            attributes.add(attr);
            return new GenericTag("published_at", 23, attributes);
        }
    }

    public static class Kinds {

        public static final Integer KIND_PRE_LONG_FORM_CONTENT = 30023;
    }
}
