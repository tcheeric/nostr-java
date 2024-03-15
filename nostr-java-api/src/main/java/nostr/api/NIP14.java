/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP14Impl.SubjectTagFactory;
import nostr.event.tag.SubjectTag;

/**
 *
 * @author eric
 */
public class NIP14 {

    /**
     * Create a subject tag
     * @param subject the subject
     */
    public static SubjectTag createSubjectTag(@NonNull String subject) {
        return new SubjectTagFactory(subject).create();
    }    
}
