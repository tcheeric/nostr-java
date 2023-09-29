/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.net.URL;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.api.factory.TagFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP23 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class LongFormContentEventFactory extends EventFactory<GenericEvent> {

        public LongFormContentEventFactory(@NonNull String content) {
            super(content);
        }

        public LongFormContentEventFactory(@NonNull Identity sender, @NonNull String content) {
            super(sender, content);
        }

        public LongFormContentEventFactory(@NonNull List<BaseTag> tags, String content) {
            super(tags, content);
        }

        public LongFormContentEventFactory(@NonNull Identity sender, @NonNull List<BaseTag> tags, String content) {
            super(sender, tags, content);
        }

        @Override
        public GenericEvent create() {
            return new GenericEvent(getSender(), Kinds.KIND_PRE_LONG_FORM_CONTENT, getTags(), getContent());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TitleTagFactory extends TagFactory {

        public TitleTagFactory(String title) {
            super("title", 23, title);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ImageTagFactory extends TagFactory {

        public ImageTagFactory(URL url) {
            super("url", 23, url.toString());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class SummaryTagFactory extends TagFactory {

        public SummaryTagFactory(String summary) {
            super("summary", 23, summary);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class PublishedAtTagFactory extends TagFactory {

        public PublishedAtTagFactory(Integer date) {
            super("created_at", 23, date.toString());
        }
    }

    public static class Kinds {

        public static final Integer KIND_PRE_LONG_FORM_CONTENT = 30023;
    }

}
