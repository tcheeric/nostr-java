/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP32Impl.Label;
import nostr.api.factory.impl.NIP32Impl.LabelTagFactory;
import nostr.api.factory.impl.NIP32Impl.NameSpace;
import nostr.api.factory.impl.NIP32Impl.NamespaceTagFactory;
import nostr.event.tag.GenericTag;

import java.util.Map;

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
        return new NamespaceTagFactory(new NameSpace(namespace)).create();
    }
    
    /**
     * 
     * @param namespace the label's namespace
     * @param label the label value
     * @param metadata optional metadata
     */
    public static GenericTag createLabelTag(@NonNull String namespace, @NonNull String label, Map<String, Object> metadata) {
        return new LabelTagFactory(new Label(new NameSpace(namespace), label, metadata)).create();
    }

    /**
     * 
     * @param namespace the label's namespace
     * @param label the label value
     */
    public static GenericTag createLabelTag(@NonNull String namespace, @NonNull String label) {
        return createLabelTag(namespace, label, null);
    }
}
