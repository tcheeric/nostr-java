/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.IEvent;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.entities.ChannelProfile;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

import static nostr.api.NIP12.createHashtagTag;

/**
 * @author eric
 */
public class NIP28 extends EventNostr {

    public NIP28(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a KIND-40 public chat channel
     *
     * @param profile the channel metadata
     */
    public NIP28 createChannelCreateEvent(@NonNull ChannelProfile profile) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CHANNEL_CREATION, StringEscapeUtils.escapeJson(profile.toString())).create();
        this.updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a KIND-42 channel message
     *
     * @param channelCreateEvent    KIND-40 channel create event
     * @param messageReplyTo        the reply tag. If present, it must be a reply to a message, else it is a root message
     * @param recommendedRelayRoot  in the scenario of a root message, the recommended relay for the root message
     * @param recommendedRelayReply in the scenario of a reply message, the recommended relay for the reply message
     * @param content               the message
     */
    public NIP28 createChannelMessageEvent(
            @NonNull GenericEvent channelCreateEvent,
            GenericEvent messageReplyTo,
            Relay recommendedRelayRoot,
            Relay recommendedRelayReply,
            @NonNull String content) {

        // 1. Validation
        if (channelCreateEvent.getKind() != Constants.Kind.CHANNEL_CREATION) {
            throw new IllegalArgumentException("The event is not a channel creation event");
        }

        // 2. Create the event
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CHANNEL_MESSAGE, content).create();

        // 3. Add the tags
        genericEvent.addTag(NIP01.createEventTag(channelCreateEvent.getId(), recommendedRelayRoot, Marker.ROOT));
        if (messageReplyTo != null) {
            genericEvent.addTag(NIP01.createEventTag(messageReplyTo.getId(), recommendedRelayReply, Marker.REPLY));
            genericEvent.addTag(NIP01.createPubKeyTag(messageReplyTo.getPubKey()));
        }

        // 4. Update the event
        this.updateEvent(genericEvent);

        return this;
    }

    /**
     * Create a KIND-42 channel root message
     *
     * @param channelCreateEvent KIND-40 channel create event
     * @param content            the message
     */
    public NIP28 createChannelMessageEvent(
            @NonNull GenericEvent channelCreateEvent,
            @NonNull Relay recommendedRelayRoot,
            @NonNull String content) {

        return createChannelMessageEvent(channelCreateEvent, null, recommendedRelayRoot, null, content);
    }

    /**
     * Create a KIND-42 channel message reply
     *
     * @param channelCreateEvent KIND-40 channel create event
     * @param eventTagReplyTo    the reply tag with the root marker
     * @param content            the message
     */
    public NIP28 createChannelMessageEvent(
            @NonNull GenericEvent channelCreateEvent,
            @NonNull GenericEvent eventTagReplyTo,
            @NonNull String content) {

        return createChannelMessageEvent(channelCreateEvent, eventTagReplyTo, null, null, content);
    }

    /**
     * Create a KIND-41 channel metadata event
     *
     * @param profile               the channel metadata
     */
    public NIP28 updateChannelMetadataEvent(@NonNull GenericEvent channelCreateEvent, @NonNull ChannelProfile profile, Relay relay) {
        return this.updateChannelMetadataEvent(channelCreateEvent, profile, null, relay);
    }

    /**
     * Create a KIND-41 channel metadata event
     * @param channelCreateEvent    KIND-40 channel create event
     * @param categories            the list of categories
     * @param profile               the channel metadata
     * @param relay                the recommended root relay
     */
    public NIP28 updateChannelMetadataEvent(@NonNull GenericEvent channelCreateEvent, @NonNull ChannelProfile profile, List<String> categories, Relay relay) {

        // 1. Validation
        if (channelCreateEvent.getKind() != Constants.Kind.CHANNEL_CREATION) {
            throw new IllegalArgumentException("The event is not a channel creation event");
        }

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CHANNEL_METADATA, StringEscapeUtils.escapeJson(profile.toString())).create();
        genericEvent.addTag(NIP01.createEventTag(channelCreateEvent.getId(), relay, Marker.ROOT));
        if (categories != null) {
            categories.stream()
                    .filter(category -> category != null && !category.isEmpty())
                    .forEach(category -> {
                        genericEvent.addTag(createHashtagTag(category));
                    });
        }
        updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a KIND-43 hide message event
     *
     * @param channelMessageEvent NIP-42 event to hide
     * @param reason              optional reason for the action
     */
    public NIP28 createHideMessageEvent(@NonNull GenericEvent channelMessageEvent, String reason) {

        if (channelMessageEvent.getKind() != Constants.Kind.CHANNEL_MESSAGE) {
            throw new IllegalArgumentException("The event is not a channel message event");
        }

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CHANNEL_HIDE_MESSAGE, Reason.fromString(reason).toString()).create();
        genericEvent.addTag(NIP01.createEventTag(channelMessageEvent.getId()));
        updateEvent(genericEvent);
        return this;
    }

    /**
     * Create a KIND-44 mute user event
     *
     * @param mutedUser the user to mute. Their messages will no longer be visible
     * @param reason    optional reason for the action
     */
    public NIP28 createMuteUserEvent(@NonNull PublicKey mutedUser, String reason) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CHANNEL_MUTE_USER, Reason.fromString(reason).toString()).create();
        genericEvent.addTag(NIP01.createPubKeyTag(mutedUser));
        updateEvent(genericEvent);
        return this;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class Reason {

        @JsonProperty("reason")
        private String value;

        public String toString() {
            try {
                return IEvent.MAPPER_BLACKBIRD.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public static Reason fromString(String reason) {
            return new Reason(reason);
        }
    }
}
