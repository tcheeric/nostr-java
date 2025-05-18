/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 *
 * @author eric
 */
public class NIP32 {
    
    /**
     * 
     * @param namespace the namespace
     */
    public static BaseTag createNameSpaceTag(@NonNull String namespace) {
        return new BaseTagFactory(Constants.Tag.NAMESPACE_CODE, namespace).create();
    }

    /**
     *
     * @param label the label value
     * @param namespace the label's namespace
     *
     */
    public static BaseTag createLabelTag(@NonNull String label, @NonNull String namespace) {
        return new BaseTagFactory(Constants.Tag.LABEL_CODE, label, namespace).create();
    }
}
