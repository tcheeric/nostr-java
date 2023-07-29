/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.event.tag.ExpirationTag;

/**
 *
 * @author eric
 */
public class NIP40 extends Api {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class ExpirationTagFactory extends TagFactory<ExpirationTag> {

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
