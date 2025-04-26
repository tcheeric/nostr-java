/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.tag.GenericTag;

/**
 *
 * @author eric
 */
public class NIP32 {
    
    /**
     * 
     * @param namespace the namespace
     */
    public static GenericTag createNameSpaceTag(@NonNull String namespace) {
        return new GenericTagFactory(Constants.Tag.NAMESPACE_CODE, namespace).create();
    }
    
/*
    */
/**
     * 
     * @param namespace the label's namespace
     * @param label the label value
     * @param metadata optional metadata
     */

    /**
     *
     * @param label the label value
     * @param namespace the label's namespace
     *
     */
    public static GenericTag createLabelTag(@NonNull String label, @NonNull String namespace) {
        return new GenericTagFactory(Constants.Tag.LABEL_CODE, label, namespace).create();
    }
}
