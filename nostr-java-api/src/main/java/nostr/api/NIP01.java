/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.event.Marker;
import nostr.event.impl.Filters;
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
public class NIP01 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TextNoteEventFactory extends EventFactory<TextNoteEvent> {

        // TextEvents attributes
        private List<EventTag> relatedEvents;
        private List<PubKeyTag> relatedPubKeys;

        public TextNoteEventFactory(PublicKey sender, String content) {
            super(sender, content);
            this.relatedEvents = new ArrayList<>();
            this.relatedPubKeys = new ArrayList<>();
        }

        @Override
        public TextNoteEvent create() {
            var event = new nostr.event.impl.TextNoteEvent(getSender(), new ArrayList<>(), getContent());
            relatedEvents.stream().forEach(e -> event.addTag(e));
            relatedPubKeys.stream().forEach(p -> event.addTag(p));
            return event;
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EventTagFactory extends TagFactory<EventTag> {

        private final IEvent relateEvent;
        private String recommendedRelayUrl;
        private Marker marker;

        public EventTagFactory(@NonNull IEvent relateEvent) {
            this.relateEvent = relateEvent;
        }

        @Override
        public EventTag create() {
            return new EventTag(relateEvent.getId(), recommendedRelayUrl, marker);
        }
        
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class PubKeyTagFactory extends TagFactory<PubKeyTag> {

        private final PublicKey publicKey;
        private String mainRelayUrl;
        private String petName;

        public PubKeyTagFactory(@NonNull PublicKey publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        public PubKeyTag create() {
            return new PubKeyTag(publicKey, mainRelayUrl, petName);
        }
        
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class FiltersFactory extends EventFactory<Filters> {

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

        public FiltersFactory() {
            super(null, null);
        }

        @Override
        public Filters create() {
            return Filters.builder().authors(authors).events(events).genericTagQueryList(genericTagQueryList).kinds(kinds).limit(limit).referencePubKeys(referencePubKeys).referencedEvents(referencedEvents).since(since).until(until).build();
        }

    }
}
