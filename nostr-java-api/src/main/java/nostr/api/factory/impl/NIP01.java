/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.factory.AbstractTagFactory;
import nostr.api.factory.EventFactory;
import nostr.api.factory.MessageFactory;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.Filters;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP01 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TextNoteEventFactory extends EventFactory<TextNoteEvent> {

        // TextEvents attributes
        public TextNoteEventFactory(@NonNull String content) {
            super(content);
        }

        public TextNoteEventFactory(@NonNull List<BaseTag> tags, @NonNull String content) {
            super(tags, content);
        }

        public TextNoteEventFactory(@NonNull IIdentity sender, @NonNull String content) {
            super(sender, content);
        }

        public TextNoteEventFactory(@NonNull IIdentity sender, @NonNull List<BaseTag> tags, @NonNull String content) {
            super(sender, tags, content);
        }

        @Override
        public TextNoteEvent create() {
            var event = new nostr.event.impl.TextNoteEvent(getSender(), getTags(), getContent());
            getTags().forEach(t -> event.addTag(t));
            return event;
        }
    }
    
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class MetadataEventFactory extends EventFactory<MetadataEvent> {

        private UserProfile profile;

        public MetadataEventFactory(@NonNull UserProfile profile) {
            super(null);
            this.profile = profile;
        }

        public MetadataEventFactory(@NonNull IIdentity sender, @NonNull UserProfile profile) {
            super(sender, null);
            this.profile = profile;
        }

        @Override
        public MetadataEvent create() {
            return new MetadataEvent(getSender(), profile);
        }        
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EventTagFactory extends AbstractTagFactory<EventTag> {

        private final String relateEventId;
        private String recommendedRelayUrl;
        private Marker marker;
        
        public EventTagFactory(@NonNull String relateEventId) {
            this.relateEventId = relateEventId;
        }

        @Override
        public EventTag create() {
            return new EventTag(relateEventId, recommendedRelayUrl, marker);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class PubKeyTagFactory extends AbstractTagFactory<PubKeyTag> {

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
    @NoArgsConstructor
    public static class FiltersFactory {

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

        public Filters create() {
            return Filters.builder().authors(authors).events(events).genericTagQueryList(genericTagQueryList).kinds(kinds).limit(limit).referencePubKeys(referencePubKeys).referencedEvents(referencedEvents).since(since).until(until).build();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class EventMessageFactory extends MessageFactory<EventMessage> {

        private final IEvent event;
        private String subscriptionId;

        public EventMessageFactory(@NonNull IEvent event) {
            this.event = event;
        }

        @Override
        public EventMessage create() {
            return new EventMessage(event, subscriptionId);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class ReqMessageFactory extends MessageFactory<ReqMessage> {

        private final String subscriptionId;
        private final Filters filters;

        @Override
        public ReqMessage create() {
            return new ReqMessage(subscriptionId, filters);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class CloseMessageFactory extends MessageFactory<CloseMessage> {

        private final String subscriptionId;

        @Override
        public CloseMessage create() {
            return new CloseMessage(subscriptionId);
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class EoseMessageFactory extends MessageFactory<EoseMessage> {

        private final String subscriptionId;

        @Override
        public EoseMessage create() {
            return new EoseMessage(subscriptionId);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    public static class NoticeMessageFactory extends MessageFactory<NoticeMessage> {

        private final String message;

        @Override
        public NoticeMessage create() {
            return new NoticeMessage(message);
        }
    }

    public static class Kinds {

        public static final Integer KIND_SET_METADATA = 0;
        public static final Integer KIND_TEXT_NOTE = 1;
        public static final Integer KIND_RECOMMEND_SERVER = 2;
    }

}
