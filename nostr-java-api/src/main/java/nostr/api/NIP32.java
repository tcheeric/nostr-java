/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.Map;
import lombok.NonNull;
import nostr.api.factory.impl.NIP32.Label;
import nostr.api.factory.impl.NIP32.LabelTagFactory;
import nostr.api.factory.impl.NIP32.NameSpace;
import nostr.api.factory.impl.NIP32.NamespaceTagFactory;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
public class NIP32 extends Nostr {
    
    /**
     * 
     * @param namespace the namespace
     * @return 
     */
    public static GenericTag createNameSpaceTag(@NonNull String namespace) {
        return new NamespaceTagFactory(new NameSpace(namespace)).create();
    }
    
    /**
     * 
     * @param namespace the label's namespace
     * @param label the label value
     * @param metadata optional metadata
     * @return 
     */
    public static GenericTag createLabelTag(@NonNull String namespace, @NonNull String label, Map<String, Object> metadata) {
        return new LabelTagFactory(new Label(new NameSpace(namespace), label, metadata)).create();
    }

    /**
     * 
     * @param namespace the label's namespace
     * @param label the label value
     * @return 
     */
    public static GenericTag createLabelTag(@NonNull String namespace, @NonNull String label) {
        return createLabelTag(namespace, label, null);
    }
}
