/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

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
public class TagFactory extends AbstractTagFactory<GenericTag> {

    @NonNull
    private final String code;
    
    @NonNull
    private final Integer nip;
    
    @NonNull
    private final String[] params;

    protected TagFactory() {
        this.code = "";
        this.nip = 0;
        this.params = new String[0];
    }

    public TagFactory(String code, Integer nip, String... params) {
        this.code = code;
        this.nip = nip;
        this.params = new String[params.length];
        System.arraycopy(params, 0, this.params, 0, params.length);
    }
    
    @Override
    public GenericTag create() {
        return GenericTag.create(code, nip, params);
    }
}
