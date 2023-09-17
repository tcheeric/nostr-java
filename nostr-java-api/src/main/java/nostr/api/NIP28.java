/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP28.ChannelCreateEventFactory;
import nostr.api.factory.impl.NIP28.ChannelMessageEventFactory;
import nostr.api.factory.impl.NIP28.ChannelMetadataEventFactory;
import nostr.api.factory.impl.NIP28.HideMessageEventFactory;
import nostr.api.factory.impl.NIP28.MuteUserEventFactory;
import nostr.base.ChannelProfile;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.ChannelMetadataEvent;
import nostr.event.impl.HideMessageEvent;
import nostr.event.impl.MuteUserEvent;

/**
 *
 * @author eric
 */
public class NIP28 {
    
    /**
     * Create a KIND-40 public chat channel
     * @param profile the channel metadata
     * @return 
     */
    public static ChannelCreateEvent createChannelCreateEvent(@NonNull ChannelProfile profile) {
        return new ChannelCreateEventFactory(profile).create();
    }
    
    /**
     * Create a KIND-42 channel message
     * @param channelCreateEvent KIND-40 channel create event
     * @param content the message
     * @return 
     */
    public static ChannelMessageEvent createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, String content) {
        return createChannelMessageEvent(channelCreateEvent, null, content);
    }

    /**
     * Create a KIND-42 channel message reply
     * @param channelCreateEvent KIND-40 channel create event
     * @param channelMessageEvent the KIND-42 channel message event 
     * @param content the message
     * @return 
     */
    public static ChannelMessageEvent createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, ChannelMessageEvent channelMessageEvent, String content) {
        return createChannelMessageEvent(channelCreateEvent, channelMessageEvent, content, null, null);
    }

    /**
     * Create a KIND-41 channel message reply while specifying the recommended relays
     * @param channelCreateEvent KIND-40 channel create event
     * @param channelMessageEvent the KIND-42 channel message event
     * @param content the message
     * @param recommendedRelayRoot the recommended relay for the KIND-40 event
     * @param recommendedRelayReply the recommended relay for the KIND-42 event
     * @return 
     */
    public static ChannelMessageEvent createChannelMessageEvent(@NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelMessageEvent channelMessageEvent, String content, Relay recommendedRelayRoot, Relay recommendedRelayReply) {
        var factory = new ChannelMessageEventFactory(channelCreateEvent, content);
        factory.setRecommendedRelayReply(recommendedRelayReply);
        factory.setRecommendedRelayRoot(recommendedRelayRoot);
        factory.setChannelMessageEvent(channelMessageEvent);
        return factory.create();
    }
    
    /**
     * Create a KIND-41 channel metadata event
     * @param channelCreateEvent the channel create event
     * @param profile the channel metadata 
     * @return 
     */
    public static ChannelMetadataEvent createChannelMetadataEvent(@NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelProfile profile) {
        return new ChannelMetadataEventFactory(channelCreateEvent, profile).create();
    }
    
    /**
     * Create a KIND-43 hide message event
     * @param channelMessageEvent NIP-42 event to hide
     * @param reason optional reason for the action
     * @return 
     */
    public static HideMessageEvent createHideMessageEvent(@NonNull ChannelMessageEvent channelMessageEvent, String reason) {
        return new HideMessageEventFactory(channelMessageEvent, reason).create();
    }
    
    /**
     * Create a KIND-44 mute user event
     * @param mutedUser the user to mute. Their messages will no longer be visible
     * @param reason optional reason for the action
     * @return 
     */
    public static MuteUserEvent createMuteUserEvent(@NonNull PublicKey mutedUser, String reason) {
        return new MuteUserEventFactory(mutedUser, reason).create();
    } 
}
