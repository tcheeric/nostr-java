package nostr.event.json.codec;

import lombok.Data;
import nostr.base.GenericTagQuery;

/**
 *
 * @author eric
 */
@Data
public class GenericTagQueryDecoder<T extends GenericTagQuery> implements FDecoder<T> {
    private final Class<T> clazz;
    private final String json;

    public GenericTagQueryDecoder(String json) {
        this.clazz = (Class<T>) GenericTagQuery.class;
        this.json = json;
    }

    @Override
    public T decode() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
