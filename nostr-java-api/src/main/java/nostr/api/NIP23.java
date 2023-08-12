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

    public static GenericEvent creatLongFormContentEvent(String content) {
        return new LongFormContentEventFactory(content).create();
    }
    
    public static GenericEvent creatLongFormContentEvent(List<BaseTag> tags, String content) {
        return new LongFormContentEventFactory(tags, content).create();
    }

    public static GenericTag createTitleTag(String title) {
        return new TitleTagFactory(title).create();
    }    

    public static GenericTag createImageTag(URL url) {
        return new ImageTagFactory(url).create();
    }


    public static GenericTag createSummaryTag(String summary) {
        return new SummaryTagFactory(summary).create();
    }

    public static GenericTag createPublishedAtTag(Integer date) {
        return new PublishedAtTagFactory(date).create();
    }
}
