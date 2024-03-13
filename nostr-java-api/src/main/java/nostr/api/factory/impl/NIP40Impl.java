/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.AbstractTagFactory;
import nostr.event.tag.ExpirationTag;

/**
 *
 * @author eric
 */
public class NIP40Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ExpirationTagFactory extends AbstractTagFactory<ExpirationTag> {

        private Integer expiration;

        public ExpirationTagFactory(@NonNull Integer expiration) {
            this.expiration = expiration;
        }

        @Override
        public ExpirationTag create() {
            return new ExpirationTag(expiration);
        }
    }

}
