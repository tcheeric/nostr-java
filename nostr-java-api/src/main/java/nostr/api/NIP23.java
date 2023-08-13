/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.net.URL;
import java.util.List;
import nostr.api.factory.impl.NIP23.ImageTagFactory;
import nostr.api.factory.impl.NIP23.LongFormContentEventFactory;
import nostr.api.factory.impl.NIP23.PublishedAtTagFactory;
import nostr.api.factory.impl.NIP23.SummaryTagFactory;
import nostr.api.factory.impl.NIP23.TitleTagFactory;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
public class NIP23 extends Nostr {

    /**
     * Create a Long-form Content event without tags
     * @param content a text in Markdown syntax
     * @return 
     */
    public static GenericEvent creatLongFormContentEvent(String content) {
        return new LongFormContentEventFactory(content).create();
    }
    
    /**
     * Create a Long-form Content event with tags
     * @param tags the note's tags
     * @param content a text in Markdown syntax
     * @return 
     */
    public static GenericEvent creatLongFormContentEvent(List<BaseTag> tags, String content) {
        return new LongFormContentEventFactory(tags, content).create();
    }

    /**
     * Create a title tag
     * @param title the article title
     * @return 
     */
    public static GenericTag createTitleTag(String title) {
        return new TitleTagFactory(title).create();
    }    

    /**
     * Create an image tag
     * @param url a URL pointing to an image to be shown along with the title
     * @return 
     */
    public static GenericTag createImageTag(URL url) {
        return new ImageTagFactory(url).create();
    }

    /**
     * Create a summary tag
     * @param summary the article summary
     * @return 
     */
    public static GenericTag createSummaryTag(String summary) {
        return new SummaryTagFactory(summary).create();
    }

    /**
     * Create a published_at tag
     * @param date the timestamp in unix seconds (stringified) of the first time the article was published
     * @return 
     */
    public static GenericTag createPublishedAtTag(Integer date) {
        return new PublishedAtTagFactory(date).create();
    }
}
