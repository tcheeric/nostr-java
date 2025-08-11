/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.net.URL;

/**
 * @author eric
 */
public class NIP23 extends EventNostr {

    public NIP23(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a Long-form Content event with tags
     *
     * @param content a text in Markdown syntax
     */
    public NIP23 creatLongFormTextNoteEvent(@NonNull String content) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.LONG_FORM_TEXT_NOTE, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    NIP23 createLongFormDraftEvent(@NonNull String content) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.LONG_FORM_DRAFT, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    public NIP23 addTitleTag(@NonNull String title) {
        getEvent().addTag(createTitleTag(title));
        return this;
    }

    public NIP23 addImageTag(@NonNull URL url) {
        getEvent().addTag(createImageTag(url));
        return this;
    }

    public NIP23 addSummaryTag(@NonNull String summary) {
        getEvent().addTag(createSummaryTag(summary));
        return this;
    }

    public NIP23 addPublishedAtTag(@NonNull Long date) {
        getEvent().addTag(createPublishedAtTag(date));
        return this;
    }


    /**
     * Create a title tag
     *
     * @param title the article title
     */
    public static BaseTag createTitleTag(@NonNull String title) {
        return new BaseTagFactory("title", title).create();
    }

    /**
     * Create an image tag
     *
     * @param url a URL pointing to an image to be shown along with the title
     */
    public static BaseTag createImageTag(@NonNull URL url) {
        return new BaseTagFactory(Constants.Tag.IMAGE_CODE, url.toString()).create();
    }

    /**
     * Create an image tag
     *
     * @param url a URL pointing to an image to be shown along with the title
     * @param size the size of the image
     */
    public static BaseTag createImageTag(@NonNull URL url, String size) {
        return new BaseTagFactory(Constants.Tag.IMAGE_CODE, url.toString(), size).create();
    }

    /**
     * Create a summary tag
     *
     * @param summary the article summary
     */
    public static BaseTag createSummaryTag(@NonNull String summary) {
        return new BaseTagFactory(Constants.Tag.SUMMARY_CODE, summary).create();
    }

    /**
     * Create a published_at tag
     *
     * @param date the timestamp in unix seconds (stringified) of the first time the article was published
     */
    public static BaseTag createPublishedAtTag(@NonNull Long date) {
        return new BaseTagFactory(Constants.Tag.PUBLISHED_AT_CODE, date.toString()).create();
    }
}
