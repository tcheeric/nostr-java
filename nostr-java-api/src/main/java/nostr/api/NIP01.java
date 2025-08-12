/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.UserProfile;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author eric
 */
public class NIP01 extends EventNostr {

    public NIP01(Identity sender) {
        setSender(sender);
    }

    /**
     * Create a NIP01 text note event without tags
     *
     * @param content the content of the note
     * @return the text note without tags
     */
    public NIP01 createTextNoteEvent(String content) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.SHORT_TEXT_NOTE, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    @Deprecated
    public NIP01 createTextNoteEvent(Identity sender, String content) {
        GenericEvent genericEvent = new GenericEventFactory(sender, Constants.Kind.SHORT_TEXT_NOTE, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    public NIP01 createTextNoteEvent(Identity sender, String content, List<PubKeyTag> recipients) {
        GenericEvent genericEvent = new GenericEventFactory<PubKeyTag>(sender, Constants.Kind.SHORT_TEXT_NOTE, recipients, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    public NIP01 createTextNoteEvent(String content, List<PubKeyTag> recipients) {
        GenericEvent genericEvent = new GenericEventFactory<PubKeyTag>(getSender(), Constants.Kind.SHORT_TEXT_NOTE, recipients, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a NIP01 text note event with recipients
     *
     * @param tags    the tags
     * @param content the content of the note
     * @return a text note event
     */
    public NIP01 createTextNoteEvent(@NonNull List<BaseTag> tags, @NonNull String content) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.SHORT_TEXT_NOTE, tags, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    public NIP01 createMetadataEvent(@NonNull UserProfile profile) {
        var sender = getSender();
        GenericEvent genericEvent = (sender != null) ? new GenericEventFactory(sender, Constants.Kind.USER_METADATA, profile.toString()).create()
                : new GenericEventFactory(Constants.Kind.USER_METADATA, profile.toString()).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a replaceable event
     *
     * @param kind    the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
     * @param content the content
     */
    public NIP01 createReplaceableEvent(Integer kind, String content) {
        var sender = getSender();
        GenericEvent genericEvent = new GenericEventFactory(sender, kind, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a replaceable event
     *
     * @param tags    the note's tags
     * @param kind    the kind (10000 <= kind < 20000 || kind == 0 || kind == 3)
     * @param content the note's content
     */
    public NIP01 createReplaceableEvent(List<BaseTag> tags, Integer kind, String content) {
        var sender = getSender();
        GenericEvent genericEvent = new GenericEventFactory(sender, kind, tags, content).create();
        this.updateEvent(genericEvent);
        return this;
    }
    /**
     * Create an ephemeral event
     *
     * @param kind    the kind (20000 <= n < 30000)
     * @param tags    the note's tags
     * @param content the note's content
     */
    public NIP01 createEphemeralEvent(List<BaseTag> tags, Integer kind, String content) {
        var sender = getSender();
        GenericEvent genericEvent = new GenericEventFactory(sender, kind, tags, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create an ephemeral event
     *
     * @param kind    the kind (20000 <= n < 30000)
     * @param content the note's content
     */
    public NIP01 createEphemeralEvent(Integer kind, String content) {
        var sender = getSender();
        GenericEvent genericEvent = new GenericEventFactory(sender, kind, content).create();
        this.updateEvent(genericEvent);
        return this;
    }


    /**
     * @param content the event's comment
     */
    public NIP01 createAddressableEvent(Integer kind, String content) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), kind, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * @param tags
     * @param kind
     * @param content
     * @return
     */
    public NIP01 createAddressableEvent(@NonNull List<GenericTag> tags, @NonNull Integer kind, String content) {
        GenericEvent genericEvent = new GenericEventFactory<GenericTag>(getSender(), kind, tags, content).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a NIP01 event tag
     *
     * @param relatedEventId the related event id
     * @return an event tag with the id of the related event
     */
    public static BaseTag createEventTag(@NonNull String relatedEventId) {
        List<String> params = List.of(relatedEventId);
        return new BaseTagFactory(Constants.Tag.EVENT_CODE, params).create();
    }

    /**
     * Create a NIP01 event tag with additional recommended relay and marker
     *
     * @param idEvent             the related event id
     * @param recommendedRelayUrl the recommended relay url
     * @param marker              the marker
     * @return an event tag with the id of the related event and optional
     * recommended relay and marker
     */
    public static BaseTag createEventTag(@NonNull String idEvent, String recommendedRelayUrl, Marker marker) {

        List<String> params = new ArrayList<>();
        params.add(idEvent);
        if (recommendedRelayUrl != null) {
            params.add(recommendedRelayUrl);
        }
        if (marker != null) {
            params.add(marker.getValue());
        }

        return new BaseTagFactory(Constants.Tag.EVENT_CODE, params).create();
    }

    /**
     * Create a NIP01 event tag with additional recommended relay and marker
     *
     * @param idEvent             the related event id
     * @param marker              the marker
     * @return an event tag with the id of the related event and optional
     * recommended relay and marker
     */
    public static BaseTag createEventTag(@NonNull String idEvent, Marker marker) {
        return createEventTag(idEvent, (String) null, marker);
    }

    /**
     * Create a NIP01 event tag with additional recommended relay and marker
     *
     * @param idEvent             the related event id
     * @param recommendedRelay    the recommended relay
     * @param marker              the marker
     * @return an event tag with the id of the related event and optional
     * recommended relay and marker
     */
    public static BaseTag createEventTag(@NonNull String idEvent, Relay recommendedRelay, Marker marker) {

        List<String> params = new ArrayList<>();
        params.add(idEvent);
        if (recommendedRelay != null) {
            params.add(recommendedRelay.getUri());
        }
        if (marker != null) {
            params.add(marker.getValue());
        }

        return new BaseTagFactory(Constants.Tag.EVENT_CODE, params).create();
    }

    /**
     * Create a NIP01 pubkey tag
     *
     * @param publicKey the associated public key
     * @return a pubkey tag with the hex representation of the associated public
     * key
     */
    public static BaseTag createPubKeyTag(@NonNull PublicKey publicKey) {
        List<String> params = new ArrayList<>();
        params.add(publicKey.toString());

        return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, params).create();
    }

    /**
     * Create a NIP01 pubkey tag with additional recommended relay and petname
     * (as defined in NIP02)
     *
     * @param publicKey    the associated public key
     * @param mainRelayUrl the recommended relay
     * @param petName      the petname
     * @return a pubkey tag with the hex representation of the associated public
     * key and the optional recommended relay and petname
     */
    // TODO - Method overloading with Relay as second parameter
    public static BaseTag createPubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
        List<String> params = new ArrayList<>();
        params.add(publicKey.toString());
        params.add(mainRelayUrl);
        params.add(petName);

        return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, params).create();
    }

    /**
     * Create a NIP01 pubkey tag with additional recommended relay and petname
     * (as defined in NIP02)
     *
     * @param publicKey    the associated public key
     * @param mainRelayUrl the recommended relay
     * @return a pubkey tag with the hex representation of the associated public
     * key and the optional recommended relay and petname
     */
    // TODO - Method overloading with Relay as second parameter
    public static BaseTag createPubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl) {
        List<String> params = new ArrayList<>();
        params.add(publicKey.toString());
        params.add(mainRelayUrl);

        return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, params).create();
    }

    /**
     * @param id
     * @return
     */
    public static BaseTag createIdentifierTag(@NonNull String id) {
        List<String> params = new ArrayList<>();
        params.add(id);

        return new BaseTagFactory(Constants.Tag.IDENTITY_CODE, params).create();
    }

    /**
     * @param kind
     * @param publicKey
     * @param idTag
     * @param relay
     * @return
     */
    public static BaseTag createAddressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, BaseTag idTag, Relay relay) {
        if (idTag != null && !idTag.getCode().equals(Constants.Tag.IDENTITY_CODE)) {
            throw new IllegalArgumentException("idTag must be an identifier tag");
        }

        List<String> params = new ArrayList<>();
        String param = kind + ":" + publicKey + ":";
        if (idTag != null) {
            if (idTag instanceof IdentifierTag) {
                param += ((IdentifierTag) idTag).getUuid();
            } else {
                // Should hopefully never happen
                throw new IllegalArgumentException("idTag must be an identifier tag");
            }
        }
        params.add(param);

        if (relay != null) {
            params.add(relay.getUri());
        }

        return new BaseTagFactory(Constants.Tag.ADDRESS_CODE, params).create();
    }

    public static BaseTag createAddressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, String id, Relay relay) {
        return createAddressTag(kind, publicKey, createIdentifierTag(id), relay);
    }

    public static BaseTag createAddressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, String id) {
        return createAddressTag(kind, publicKey, createIdentifierTag(id), null);
    }

    /**
     * Create an event message to send events requested by clients
     *
     * @param event          the related event
     * @param subscriptionId the related subscription id
     * @return an event message
     */
    public static EventMessage createEventMessage(@NonNull GenericEvent event, String subscriptionId) {
        return Optional.ofNullable(subscriptionId)
                .map(subId -> new EventMessage(event, subId))
                .orElse(new EventMessage(event));
    }

    /**
     * Create a REQ message to request events and subscribe to new updates
     *
     * @param subscriptionId the subscription id
     * @param filtersList    the filters list
     * @return a REQ message
     */
    public static ReqMessage createReqMessage(@NonNull String subscriptionId, @NonNull List<Filters> filtersList) {
        return new ReqMessage(subscriptionId, filtersList);
    }

    /**
     * Create a CLOSE message to stop previous subscriptions
     *
     * @param subscriptionId the subscription id
     * @return a CLOSE message
     */
    public static CloseMessage createCloseMessage(@NonNull String subscriptionId) {
        return new CloseMessage(subscriptionId);
    }

    /**
     * Create an EOSE message to indicate the end of stored events and the
     * beginning of events newly received in real-time
     *
     * @param subscriptionId the subscription id
     * @return an EOSE message
     */
    public static EoseMessage createEoseMessage(@NonNull String subscriptionId) {
        return new EoseMessage(subscriptionId);
    }

    /**
     * Create a NOTICE message to send human-readable error messages or other
     * things to clients.
     *
     * @param message the human-readable message to send to the client
     * @return a NOTICE message
     */
    public static NoticeMessage createNoticeMessage(@NonNull String message) {
        return new NoticeMessage(message);
    }
}
