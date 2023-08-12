/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP01.CloseMessageFactory;
import nostr.api.factory.impl.NIP01.EoseMessageFactory;
import nostr.api.factory.impl.NIP01.EventMessageFactory;
import nostr.api.factory.impl.NIP01.EventTagFactory;
import nostr.api.factory.impl.NIP01.FiltersFactory;
import nostr.api.factory.impl.NIP01.NoticeMessageFactory;
import nostr.api.factory.impl.NIP01.PubKeyTagFactory;
import nostr.api.factory.impl.NIP01.ReqMessageFactory;
import nostr.api.factory.impl.NIP01.TextNoteEventFactory;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.Filters;
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

/**
 *
 * @author eric
 */
public class NIP01 extends Nostr {

    public static TextNoteEvent createTextNoteEvent(String content) {
        return new TextNoteEventFactory(content).create();
    }
    
    public static TextNoteEvent createTextNoteEvent(List<BaseTag> tags, String content) {
        return new TextNoteEventFactory(tags, content).create();
    }

    public static EventTag createEventTag(IEvent relateEvent) {
        return new EventTagFactory(relateEvent).create();        
    }
    
    public static EventTag createEventTag(IEvent relateEvent, String recommendedRelayUrl, Marker marker) {
        var result = new EventTagFactory(relateEvent).create();
        result.setMarker(marker);
        result.setRecommendedRelayUrl(recommendedRelayUrl);
        return result;
    }
    
    public static PubKeyTag createPubKeyTag(PublicKey publicKey) {
        return new PubKeyTagFactory(publicKey).create();
    }

    public static PubKeyTag createPubKeyTag(PublicKey publicKey, String mainRelayUrl, String petName) {
        var result = new PubKeyTagFactory(publicKey).create();
        result.setMainRelayUrl(mainRelayUrl);
        result.setPetName(petName);
        return result;
    }

    public static Filters createFilters(EventList events, PublicKeyList authors, KindList kinds, EventList referencedEvents, PublicKeyList referencePubKeys, Long since, Long until, Integer limit, GenericTagQueryList genericTagQueryList) {
        var factory = new FiltersFactory();
        factory.setAuthors(authors);
        factory.setEvents(events);
        factory.setGenericTagQueryList(genericTagQueryList);
        factory.setKinds(kinds);
        factory.setLimit(limit);
        factory.setReferencePubKeys(referencePubKeys);
        factory.setReferencedEvents(referencedEvents);
        factory.setSince(since);
        factory.setUntil(until);
        return factory.create();
    }
    
    public static EventMessage createEventMessage(IEvent event, String subscriptionId) {
        var result = new EventMessageFactory(event).create();
        result.setSubscriptionId(subscriptionId);
        return result;
    }
    
    public static ReqMessage createReqMessage(String subscriptionId, Filters filters) {
        return new ReqMessageFactory(subscriptionId, filters).create();
    }
    
    public static CloseMessage createCloseMessage(String subscriptionId) {
        return new CloseMessageFactory(subscriptionId).create();
    }

    public static EoseMessage createEoseMessage(String subscriptionId) {
        return new EoseMessageFactory(subscriptionId).create();
    }

    public static NoticeMessage createNoticeMessage(String message) {
        return new NoticeMessageFactory(message).create();
    }
}
