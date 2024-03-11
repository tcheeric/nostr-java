/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.UserProfile;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP05Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class InternetIdentifierMetadataEventFactory extends EventFactory<InternetIdentifierMetadataEvent> {

        private final UserProfile profile;

        public InternetIdentifierMetadataEventFactory(@NonNull Identity sender, @NonNull UserProfile profile) {
            super(sender, null);
            this.profile = profile;
        }

        @Override
        public InternetIdentifierMetadataEvent create() {
            return new InternetIdentifierMetadataEvent(getSender(), profile);
        }
    }
}
