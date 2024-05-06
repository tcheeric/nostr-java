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
import nostr.event.impl.HideMessageEvent;
import nostr.event.impl.MuteUserEvent;
import nostr.id.Identity;

import static nostr.util.NostrUtil.escapeJsonString;

/**
 *
 * @author eric
 */
public class NIP28Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ChannelCreateEventFactory extends EventFactory<ChannelCreateEvent> {

        private final ChannelProfile profile;

        public ChannelCreateEventFactory(@NonNull ChannelProfile profile) {
            this.profile = profile;
        }

        public ChannelCreateEventFactory(Identity sender, @NonNull ChannelProfile profile) {
            super(sender, null);
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

        public ChannelMessageEventFactory(@NonNull Identity sender, @NonNull ChannelCreateEvent rootEvent, @NonNull String content) {
            super(sender, content);
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
            this.channelCreateEvent = channelCreateEvent;
            this.profile = profile;
        }

        public ChannelMetadataEventFactory(@NonNull Identity sender, @NonNull ChannelCreateEvent channelCreateEvent, @NonNull ChannelProfile profile) {
            super(sender, null);
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

        public HideMessageEventFactory(@NonNull ChannelMessageEvent channelMessageEvent, @NonNull String reason) {
            this.channelMessageEvent = channelMessageEvent;
            this.reason = reason;
        }

        public HideMessageEventFactory(@NonNull Identity sender, @NonNull ChannelMessageEvent channelMessageEvent, @NonNull String reason) {
            super(sender, null);
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

        public MuteUserEventFactory(@NonNull PublicKey mutedUser, @NonNull String reason) {
            this.mutedUser = mutedUser;
            this.reason = reason;
        }

        public MuteUserEventFactory(@NonNull Identity sender, @NonNull PublicKey mutedUser, @NonNull String reason) {
            super(sender, null);
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
