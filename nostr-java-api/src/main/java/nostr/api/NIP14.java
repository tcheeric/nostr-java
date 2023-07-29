/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import nostr.api.factory.TagFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.event.tag.SubjectTag;

/**
 *
 * @author eric
 */
public class NIP14 extends Api {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class SubjectTagFactory extends TagFactory<SubjectTag> {

        private final String subject;

        public SubjectTagFactory(String subject) {
            this.subject = subject;
        }

        @Override
        public SubjectTag create() {
            return new SubjectTag(subject);
        }

    }
}
