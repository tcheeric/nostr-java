/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.NIP01.CloseMessageFactory;
import nostr.api.factory.impl.NIP01.EoseMessageFactory;
import nostr.api.factory.impl.NIP01.EventMessageFactory;
import nostr.api.factory.impl.NIP01.EventTagFactory;
import nostr.api.factory.impl.NIP01.FiltersFactory;
import nostr.api.factory.impl.NIP01.MetadataEventFactory;
import nostr.api.factory.impl.NIP01.NoticeMessageFactory;
import nostr.api.factory.impl.NIP01.PubKeyTagFactory;
import nostr.api.factory.impl.NIP01.ReqMessageFactory;
import nostr.api.factory.impl.NIP01.TextNoteEventFactory;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.Filters;
import nostr.event.impl.MetadataEvent;
import nostr.event.impl.ParameterizedReplaceableEvent;
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
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP01 extends Nostr {

    /**
     * Create a NIP01 text note event without tags
     *
     * @param content the content of the note
     * @return the text note without tags
     */
    public static TextNoteEvent createTextNoteEvent(@NonNull String content) {
        return new TextNoteEventFactory(content).create();
    }

    public static TextNoteEvent createTextNoteEvent(@NonNull IIdentity sender, @NonNull String content) {
        return new TextNoteEventFactory(sender, content).create();
    }

    /**
     * Create a NIP01 text note event with tags
     *
     * @param tags the note tags
     * @param content the content of the note
     * @return a text note event
     */
    public static TextNoteEvent createTextNoteEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
        return new TextNoteEventFactory(tags, content).create();
    }

    public static TextNoteEvent createTextNoteEvent(@NonNull IIdentity sender, @NonNull List<BaseTag> tags, @NonNull String content) {
        return new TextNoteEventFactory(sender, tags, content).create();
    }

    /**
     * Create a NIP01 metadata event
     *
     * @param profile the associated profile
     * @return a metadata event associated for the profile
     */
    public static MetadataEvent createMetadataEvent(@NonNull UserProfile profile) {
        return new MetadataEventFactory(profile).create();
    }

    public static MetadataEvent createMetadataEvent(@NonNull IIdentity sender, @NonNull UserProfile profile) {
        return new MetadataEventFactory(sender, profile).create();
    }

    /**
     * Create a NIP01 event tag
     *
     * @param relateEventId the related event id
     * @return an event tag with the id of the related event
     */
    public static EventTag createEventTag(@NonNull String relateEventId) {
        return new EventTagFactory(relateEventId).create();
    }

    /**
     * Create a NIP01 event tag with additional recommended relay and marker
     *
     * @param relateEventId the related event id
     * @param recommendedRelayUrl the recommended relay
     * @param marker the marker
     * @return an event tag with the id of the related event and optional
     * recommended relay and marker
     */
    public static EventTag createEventTag(@NonNull String relateEventId, String recommendedRelayUrl, Marker marker) {
        var result = new EventTagFactory(relateEventId).create();
        result.setMarker(marker);
        result.setRecommendedRelayUrl(recommendedRelayUrl);
        return result;
    }

    /**
     * Create a NIP01 pubkey tag
     *
     * @param publicKey the associated public key
     * @return a pubkey tag with the hex representation of the associated public
     * key
     */
    public static PubKeyTag createPubKeyTag(@NonNull PublicKey publicKey) {
        return new PubKeyTagFactory(publicKey).create();
    }

    /**
     * Create a NIP01 pubkey tag with additional recommended relay and petname
     * (as defined in NIP02)
     *
     * @param publicKey the associated public key
     * @param mainRelayUrl the recommended relay
     * @param petName the petname
     * @return a pubkey tag with the hex representation of the associated public
     * key and the optional recommended relay and petname
     */
    public static PubKeyTag createPubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
        var result = new PubKeyTagFactory(publicKey).create();
        result.setMainRelayUrl(mainRelayUrl);
        result.setPetName(petName);
        return result;
    }

    /**
     * Create a NIP01 filters object (all parameters are optional)
     *
     * @param events a list of event
     * @param authors a list of pubkeys or prefixes, the pubkey of an event must
     * be one of these
     * @param kinds a list of a kind numbers
     * @param referencedEvents a list of event ids that are referenced in an "e"
     * tag
     * @param referencePubKeys a list of pubkeys that are referenced in a "p"
     * tag
     * @param since an integer unix timestamp in seconds, events must be newer
     * than this to pass
     * @param until an integer unix timestamp in seconds, events must be older
     * than this to pass
     * @param limit maximum number of events to be returned in the initial query
     * @param genericTagQueryList a generic tag query list
     * @return a filters object
     */
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

    /**
     * Create an event message to send events requested by clients
     *
     * @param event the related event
     * @param subscriptionId the related subscription id
     * @return an event message
     */
    public static EventMessage createEventMessage(@NonNull IEvent event, @NonNull String subscriptionId) {
        var result = new EventMessageFactory(event).create();
        result.setSubscriptionId(subscriptionId);
        return result;
    }

    /**
     * Create a REQ message to request events and subscribe to new updates
     *
     * @param subscriptionId the subscription id
     * @param filters the filters object
     * @return a REQ message
     */
    public static ReqMessage createReqMessage(@NonNull String subscriptionId, @NonNull Filters filters) {
        return new ReqMessageFactory(subscriptionId, filters).create();
    }

    /**
     * Create a CLOSE message to stop previous subscriptions
     *
     * @param subscriptionId the subscription id
     * @return a CLOSE message
     */
    public static CloseMessage createCloseMessage(@NonNull String subscriptionId) {
        return new CloseMessageFactory(subscriptionId).create();
    }

    /**
     * Create an EOSE message to indicate the end of stored events and the
     * beginning of events newly received in real-time
     *
     * @param subscriptionId the subscription id
     * @return an EOSE message
     */
    public static EoseMessage createEoseMessage(@NonNull String subscriptionId) {
        return new EoseMessageFactory(subscriptionId).create();
    }

    /**
     * Create a NOTICE message to send human-readable error messages or other
     * things to clients.
     *
     * @param message the human-readable message to send to the client
     * @return a NOTICE message
     */
    public static NoticeMessage createNoticeMessage(@NonNull String message) {
        return new NoticeMessageFactory(message).create();
    }

    /**
     *
     * @param kind
     * @param comment
     * @return
     */
    public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(@NonNull Integer kind, String comment) {
        return NIP33.createParameterizedReplaceableEvent(kind, comment);
    }

    /**
     *
     * @param tags
     * @param kind
     * @param comment
     * @return
     */
    public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(@NonNull List<BaseTag> tags, @NonNull Integer kind, String comment) {
        return NIP33.createParameterizedReplaceableEvent(tags, kind, comment);
    }

    /**
     *
     * @param id
     * @return
     */
    public static IdentifierTag createIdentifierTag(@NonNull String id) {
        return NIP33.createIdentifierTag(id);
    }

    /**
     *
     * @param kind
     * @param publicKey
     * @param idTag
     * @param relay
     * @return
     */
    public static AddressTag createAddressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, @NonNull IdentifierTag idTag, Relay relay) {
        return NIP33.createAddressTag(kind, publicKey, idTag, relay);
    }
}
