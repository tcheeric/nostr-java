/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.ContactListEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.OtsEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;

/**
 *
 * @author eric
 */
@Deprecated(forRemoval = true)
public class Events {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NIP01 extends EventFactory<TextNoteEvent> {

        // TextEvents attributes
        private List<EventTag> relatedEvents;
        private List<PubKeyTag> relatedPubKeys;

        // Filters attributes
        private EventList events;
        private PublicKeyList authors;
        private KindList kinds;
        private EventList referencedEvents;
        private PublicKeyList referencePubKeys;
        private Long since;
        private Long until;
        private Integer limit;
        private GenericTagQueryList genericTagQueryList;

        public NIP01(PublicKey sender, String content) {
            super(sender, content);
            this.relatedEvents = new ArrayList<>();
            this.relatedPubKeys = new ArrayList<>();
        }

        public NIP01() {
            this(null, null);
        }

        @Override
        public TextNoteEvent createEvent() {
            var event = new TextNoteEvent(getSender(), new ArrayList<>(), getContent());
            relatedEvents.stream().forEach(e -> event.addTag(e));
            relatedPubKeys.stream().forEach(p -> event.addTag(p));
            return event;
        }

        public Filters createFilters() {
            return Filters.builder().authors(authors).events(events).genericTagQueryList(genericTagQueryList).kinds(kinds).limit(limit).referencePubKeys(referencePubKeys).referencedEvents(referencedEvents).since(since).until(until).build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NIP02 extends EventFactory<ContactListEvent> {

        private List<PubKeyTag> relatedPubKeys;

        public NIP02(PublicKey sender, String content) {
            super(sender, content);
            this.relatedPubKeys = new ArrayList<>();
        }

        @Override
        public ContactListEvent createEvent() {
            return new ContactListEvent(getSender(), relatedPubKeys);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NIP03 extends EventFactory<OtsEvent> {

        private final String ots;
        private List<EventTag> relatedEvents;
        private List<PubKeyTag> relatedPubKeys;

        public NIP03(PublicKey sender, String content, String ots) {
            super(sender, content);
            this.ots = ots;
            this.relatedEvents = new ArrayList<>();
            this.relatedPubKeys = new ArrayList<>();
        }

        @Override
        public OtsEvent createEvent() {
            var event = new OtsEvent(getSender(), new ArrayList<>(), getContent(), ots);
            relatedEvents.stream().forEach(e -> event.addTag(e));
            relatedPubKeys.stream().forEach(p -> event.addTag(p));
            return event;
        }

    }

}
