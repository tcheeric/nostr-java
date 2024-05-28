
package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * @author squirrel
 * @param <IElement>
 */
public interface IEncoder<IElement> {
    ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    
    String encode();
}
