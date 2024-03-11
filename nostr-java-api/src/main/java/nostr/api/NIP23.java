/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP23Impl;
import nostr.api.factory.impl.NIP23Impl.*;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

import java.net.URL;
import java.util.List;

/**
 * @author eric
 */
public class NIP23<T extends GenericEvent> extends EventNostr<T> {

    public NIP23(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a Long-form Content event without tags
     *
     * @param content a text in Markdown syntax
     * @return
     */
    public static GenericEvent creatLongFormContentEvent(@NonNull String content) {
        return new LongFormContentEventFactory(content).create();
    }

    /**
     * Create a Long-form Content event with tags
     *
     * @param tags    the note's tags
     * @param content a text in Markdown syntax
     * @return
     */
    public NIP23<T> creatLongFormContentEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
        var factory = new NIP23Impl.LongFormContentEventFactory(getSender(), content);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    public NIP23<T> addTitleTag(@NonNull String title) {
        getEvent().addTag(createTitleTag(title));
        return this;
    }

    public NIP23<T> addImageTag(@NonNull URL url) {
        getEvent().addTag(createImageTag(url));
        return this;
    }

    public NIP23<T> addSummaryTag(@NonNull String summary) {
        getEvent().addTag(createSummaryTag(summary));
        return this;
    }

    public NIP23<T> addPublishedAtTag(@NonNull Integer date) {
        getEvent().addTag(createPublishedAtTag(date));
        return this;
    }

    public NIP23<T> addEventTag(@NonNull EventTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    public NIP23<T> addAddressTag(@NonNull AddressTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    /**
     * Create a title tag
     *
     * @param title the article title
     * @return
     */
    public static GenericTag createTitleTag(@NonNull String title) {
        return new TitleTagFactory(title).create();
    }

    /**
     * Create an image tag
     *
     * @param url a URL pointing to an image to be shown along with the title
     * @return
     */
    public static GenericTag createImageTag(@NonNull URL url) {
        return new ImageTagFactory(url).create();
    }

    /**
     * Create a summary tag
     *
     * @param summary the article summary
     * @return
     */
    public static GenericTag createSummaryTag(@NonNull String summary) {
        return new SummaryTagFactory(summary).create();
    }

    /**
     * Create a published_at tag
     *
     * @param date the timestamp in unix seconds (stringified) of the first time the article was published
     * @return
     */
    public static GenericTag createPublishedAtTag(@NonNull Integer date) {
        return new PublishedAtTagFactory(date).create();
    }
}
