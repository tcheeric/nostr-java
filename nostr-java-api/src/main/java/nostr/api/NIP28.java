/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP28Impl;
import nostr.api.factory.impl.NIP28Impl.ChannelMessageEventFactory;
import nostr.api.factory.impl.NIP28Impl.ChannelMetadataEventFactory;
import nostr.api.factory.impl.NIP28Impl.HideMessageEventFactory;
import nostr.api.factory.impl.NIP28Impl.MuteUserEventFactory;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * @author eric
 */
public class NIP28<T extends GenericEvent> extends EventNostr<T> {

    public NIP28(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create a KIND-40 public chat channel
     *
     * @param profile the channel metadata
     * @return
     */
    public NIP28<T> createChannelCreateEvent(@NonNull ChannelProfile profile) {
        var factory = new NIP28Impl.ChannelCreateEventFactory(getSender(), profile);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Create a KIND-42 channel message
     *
     * @param channelCreateEvent KIND-40 channel create event
     * @param content            the message
     * @return
     */
    public NIP28<T> createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, String content) {
        var factory = new NIP28Impl.ChannelMessageEventFactory(getSender(), channelCreateEvent, content);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Create a KIND-42 channel message reply
     *
     * @param channelCreateEvent  KIND-40 channel create event
     * @param channelMessageEvent the KIND-42 channel message event
     * @param content             the message
     * @return
     */
    public NIP28<T> createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, ChannelMessageEvent channelMessageEvent, String content) {
        return createChannelMessageEvent(channelCreateEvent, channelMessageEvent, content, null, null);
    }

    /**
     * Create a KIND-41 channel message reply while specifying the recommended relays
     *
     * @param channelCreateEvent    KIND-40 channel create event
     * @param channelMessageEvent   the KIND-42 channel message event
     * @param content               the message
     * @param recommendedRelayRoot  the recommended relay for the KIND-40 event
     * @param recommendedRelayReply the recommended relay for the KIND-42 event
     * @return
     */
    public NIP28<T> createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelMessageEvent channelMessageEvent, String content, Relay recommendedRelayRoot, Relay recommendedRelayReply) {
        var factory = new NIP28Impl.ChannelMessageEventFactory(getSender(), channelCreateEvent, content);
        factory.setRecommendedRelayReply(recommendedRelayReply);
        factory.setRecommendedRelayRoot(recommendedRelayRoot);
        factory.setChannelMessageEvent(channelMessageEvent);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Create a KIND-41 channel metadata event
     *
     * @param channelCreateEvent the channel create event
     * @param profile            the channel metadata
     * @return
     */
    public NIP28<T> createChannelMetadataEvent(@NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelProfile profile) {
        var factory = new NIP28Impl.ChannelMetadataEventFactory(getSender(), channelCreateEvent, profile);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Create a KIND-43 hide message event
     *
     * @param channelMessageEvent NIP-42 event to hide
     * @param reason              optional reason for the action
     * @return
     */
    public NIP28<T> createHideMessageEvent(@NonNull ChannelMessageEvent channelMessageEvent, String reason) {
        var factory = new NIP28Impl.HideMessageEventFactory(getSender(), channelMessageEvent, reason);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * Create a KIND-44 mute user event
     *
     * @param mutedUser the user to mute. Their messages will no longer be visible
     * @param reason    optional reason for the action
     * @return
     */
    public NIP28<T> createMuteUserEvent(@NonNull PublicKey mutedUser, String reason) {
        var factory = new NIP28Impl.MuteUserEventFactory(getSender(), mutedUser, reason);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }
}
