
package nostr.base;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author squirrel
 */
@Data
@Builder
public class GenericTagQuery {
    
    private final Character tagName;
    private final List<String> value;
        
}
