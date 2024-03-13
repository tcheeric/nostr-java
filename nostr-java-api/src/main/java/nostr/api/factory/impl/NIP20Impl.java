/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.MessageFactory;
import nostr.base.IEvent;
import nostr.event.message.OkMessage;

/**
 *
 * @author eric
 */
public class NIP20Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OkMessageFactory extends MessageFactory<OkMessage> {

        private final IEvent event;
        private final boolean flag;
        private final String message;

        public OkMessageFactory(@NonNull IEvent event, boolean flag, @NonNull String message) {
            this.event = event;
            this.flag = flag;
            this.message = message;
        }

        @Override
        public OkMessage create() {
            return new OkMessage(event.getId(), flag, message);
        }
    }

}
