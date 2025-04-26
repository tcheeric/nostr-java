/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.tag.GenericTag;

import java.util.List;

/**
 *
 * @author eric
 */
public class NIP14 {

    /**
     * Create a subject tag
     * @param subject the subject
     */
    public static GenericTag createSubjectTag(@NonNull String subject) {
        return new GenericTagFactory(Constants.Tag.SUBJECT_CODE, List.of(subject)).create();
    }    
}
