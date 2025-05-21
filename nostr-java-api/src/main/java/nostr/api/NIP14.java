/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

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
    public static BaseTag createSubjectTag(@NonNull String subject) {
        return new BaseTagFactory(Constants.Tag.SUBJECT_CODE, List.of(subject)).create();
    }    
}
