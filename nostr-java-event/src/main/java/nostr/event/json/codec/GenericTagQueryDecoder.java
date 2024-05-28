package nostr.event.json.codec;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.FDecoder;
import nostr.base.GenericTagQuery;

/**
 *
 * @author eric
 */
@AllArgsConstructor
@Data
public class GenericTagQueryDecoder<T extends GenericTagQuery> implements FDecoder<T> {

    private final String json;
    
    @Override
    public T decode(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
