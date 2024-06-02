/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP01Impl.AddressTagFactory;
import nostr.api.factory.impl.NIP01Impl.CloseMessageFactory;
import nostr.api.factory.impl.NIP01Impl.EoseMessageFactory;
import nostr.api.factory.impl.NIP01Impl.EphemeralEventFactory;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.api.factory.impl.NIP01Impl.EventTagFactory;
import nostr.api.factory.impl.NIP01Impl.IdentifierTagFactory;
import nostr.api.factory.impl.NIP01Impl.MetadataEventFactory;
import nostr.api.factory.impl.NIP01Impl.NoticeMessageFactory;
import nostr.api.factory.impl.NIP01Impl.ParameterizedReplaceableEventFactory;
import nostr.api.factory.impl.NIP01Impl.PubKeyTagFactory;
import nostr.api.factory.impl.NIP01Impl.ReplaceableEventFactory;
import nostr.api.factory.impl.NIP01Impl.ReqMessageFactory;
import nostr.api.factory.impl.NIP01Impl.TextNoteEventFactory;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.base.UserProfile;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.Marker;
import nostr.event.NIP01Event;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.List;

/**
 *
 * @author eric
 */
public class NIP01<T extends NIP01Event> extends EventNostr<T> {
	
	public NIP01(@NonNull Identity sender) {
		setSender(sender);
	}

    /**
     * Create a NIP01 text note event without tags
     *
     * @param content the content of the note
     * @return the text note without tags
     */
	public NIP01<T> createTextNoteEvent(@NonNull String content) {
		var event = new TextNoteEventFactory(getSender(), content).create();
		this.setEvent((T) event);

		return this;
    }

	public NIP01<T> createTextNoteEvent(@NonNull Identity sender, @NonNull String content) {
		var event = new TextNoteEventFactory(sender, content).create();
		this.setEvent((T) event);

		return this;
    }

    /**
     * Create a NIP01 text note event with tags
     *
     * @param tags the note tags
     * @param content the content of the note
     * @return a text note event
     */
    public NIP01<T> createTextNoteEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
      setEvent((T) new TextNoteEventFactory(getSender(), tags, content).create());
      return this;
    }

    public NIP01<T> createMetadataEvent(@NonNull UserProfile profile) {
    	var sender = getSender();
    	var event = (sender!=null) ? new MetadataEventFactory(sender, profile).create() : new MetadataEventFactory(profile).create();
        
        this.setEvent((T) event);
        return this;
    }

    /**
     * Create a replaceable event
     * @param kind the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
     * @param content the content
     */
    public NIP01<T> createReplaceableEvent(@NonNull Integer kind, String content) {
    	var event = new ReplaceableEventFactory(getSender(), kind, content).create();
        
        this.setEvent((T) event);
        return this;
    }
    
    /**
     * Create a replaceable event
     * @param tags the note's tags
     * @param kind the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
     * @param content the note's content
     */
    public NIP01<T> createReplaceableEvent(@NonNull List<BaseTag> tags, @NonNull Integer kind, String content) {
    	var event = new ReplaceableEventFactory(getSender(), tags, kind, content).create();
        
        this.setEvent((T) event);
        return this;
    }

    /**
     * Create an ephemeral event
     * @param kind the kind (20000 <= n < 30000)
     * @param content the note's content
     */
    public NIP01<T> createEphemeralEvent(@NonNull Integer kind, String content) {
    	var event = new EphemeralEventFactory(getSender(), kind, content).create();   
        
        this.setEvent((T) event);
        return this;     
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
     * @param genericTagQuery a generic tag query
     * @return a filters object
     */
    @Deprecated(forRemoval = true)
    public static Filters createFilters(List<GenericEvent> events, List<PublicKey> authors, List<Kind> kinds, List<GenericEvent> referencedEvents, List<PublicKey> referencePubKeys, Long since, Long until, Integer limit, GenericTagQuery genericTagQuery) {
        return Filters.builder()
        		.authors(authors)
        		.events(events)
        		.genericTagQuery(genericTagQuery)
        		.kinds(kinds).limit(limit)
        		.referencePubKeys(referencePubKeys)
        		.referencedEvents(referencedEvents)
        		.since(since)
        		.until(until)
        		.build();
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
     * @param filtersList the filters list
     * @return a REQ message
     */
    public static ReqMessage createReqMessage(@NonNull String subscriptionId, @NonNull List<Filters> filtersList) {
        return new ReqMessageFactory(subscriptionId, filtersList).create();
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
     * @param comment the event's comment
     */
    public NIP01<T> createParameterizedReplaceableEvent(@NonNull Integer kind, String comment) {
    	var event = new ParameterizedReplaceableEventFactory(getSender(), kind, comment).create();
        
        this.setEvent((T) event);
        return this;
    }
    
    /**
     * 
     * @param tags
     * @param kind
     * @param comment
     * @return 
     */
    public NIP01<T> createParameterizedReplaceableEvent(@NonNull List<BaseTag> tags, @NonNull Integer kind, String comment) {
    	var event = new ParameterizedReplaceableEventFactory(getSender(), tags, kind, comment).create();
        
        this.setEvent((T) event);
        return this;
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public static IdentifierTag createIdentifierTag(@NonNull String id) {
        return new IdentifierTagFactory(id).create();
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
        var result = new AddressTagFactory(publicKey).create();
        result.setIdentifierTag(idTag);
        result.setKind(kind);
        result.setRelay(relay);
        return result;
    }    
}
