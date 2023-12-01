package nostr.event.json.codec;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.GenericTagQuery;
import nostr.base.IDecoder;

/**
 *
 * @author eric
 */
@AllArgsConstructor
@Data
public class GenericTagQueryDecoder implements IDecoder<GenericTagQuery> {

    private final String json;
    
    @Override
    public GenericTagQuery decode() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
