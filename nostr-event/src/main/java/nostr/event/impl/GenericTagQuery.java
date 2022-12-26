
package nostr.event.impl;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import nostr.base.annotation.NIPSupport;

/**
 *
 * @author squirrel
 */
@Data
@Builder
@NIPSupport(value = 12, description = "Generic Tag Queries")
public class GenericTagQuery {
    
    private final Character tagName;
    private final List<String> value;
        
}
