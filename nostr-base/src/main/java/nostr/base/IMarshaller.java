
package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
public interface IMarshaller {
    public static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    
    public abstract String marshall() throws NostrException;
    
    public String toJson() throws NostrException;
}
