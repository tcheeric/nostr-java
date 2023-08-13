/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.ChannelProfile;
import nostr.base.ContentReason;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.ChannelMessageEvent;
import nostr.event.impl.ChannelMetadataEvent;
import static nostr.event.impl.GenericEvent.escapeJsonString;
import nostr.event.impl.HideMessageEvent;
import nostr.event.impl.MuteUserEvent;

/**
 *
 * @author eric
 */
public class NIP28 {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ChannelCreateEventFactory extends EventFactory<ChannelCreateEvent> {

        private final ChannelProfile profile;

        public ChannelCreateEventFactory(@NonNull ChannelProfile profile) {
            super(null);
            this.profile = profile;
        }

        @Override
        public ChannelCreateEvent create() {
            return new ChannelCreateEvent(getSender(), profile);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ChannelMessageEventFactory extends EventFactory<ChannelMessageEvent> {

        private final ChannelCreateEvent rootEvent;
        private ChannelMessageEvent channelMessageEvent;
        private Relay recommendedRelayRoot;
        private Relay recommendedRelayReply;

        public ChannelMessageEventFactory(ChannelCreateEvent rootEvent, String content) {
            super(content);
            this.rootEvent = rootEvent;
        }

        @Override
        public ChannelMessageEvent create() {
            return new ChannelMessageEvent(getSender(), rootEvent, channelMessageEvent, getContent(), recommendedRelayRoot, recommendedRelayReply);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ChannelMetadataEventFactory extends EventFactory<ChannelMetadataEvent> {

        private final ChannelProfile profile;
        private final ChannelCreateEvent channelCreateEvent;

        public ChannelMetadataEventFactory(@NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelProfile profile) {
            super(null);
            this.channelCreateEvent = channelCreateEvent;
            this.profile = profile;
        }

        @Override
        public ChannelMetadataEvent create() {
            return new ChannelMetadataEvent(getSender(), channelCreateEvent, profile);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class HideMessageEventFactory extends EventFactory<HideMessageEvent> {

        private final ChannelMessageEvent channelMessageEvent;
        private String reason;

        public HideMessageEventFactory(ChannelMessageEvent channelMessageEvent, String reason) {
            super(null);
            this.channelMessageEvent = channelMessageEvent;
            this.reason = reason;
        }

        @Override
        public String getContent() {
            if (reason != null) {
                ContentReason contentReason = new ContentReason(reason);
                return escapeJsonString(contentReason.toString());
            }

            return null;
        }

        @Override
        public HideMessageEvent create() {
            return new HideMessageEvent(getSender(), channelMessageEvent, getContent());
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class MuteUserEventFactory extends EventFactory<MuteUserEvent> {

        private final PublicKey mutedUser;
        private String reason;

        public MuteUserEventFactory(PublicKey mutedUser, String reason) {
            super(null);
            this.mutedUser = mutedUser;
            this.reason = reason;
        }

        @Override
        public String getContent() {
            if (reason != null) {
                ContentReason contentReason = new ContentReason(reason);
                return escapeJsonString(contentReason.toString());
            }

            return null;
        }

        @Override
        public MuteUserEvent create() {
            return new MuteUserEvent(getSender(), mutedUser, getContent());
        }
    }
}
