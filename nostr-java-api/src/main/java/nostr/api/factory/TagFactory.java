/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.impl.GenericTag;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class TagFactory extends AbstractTagFactory<GenericTag> {

    @NonNull
    private final String code;
    
    @NonNull
    private final Integer nip;
    
    @NonNull
    private final String param;
    
    @Override
    public GenericTag create() {
        return GenericTag.create(code, nip, param);
    }
}
