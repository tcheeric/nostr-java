
package nostr.event.impl;

import com.tcheeric.nostr.base.annotation.NIPSupport;
import java.util.List;
import lombok.Builder;
import lombok.Data;

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
