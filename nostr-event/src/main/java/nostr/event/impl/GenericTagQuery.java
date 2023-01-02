
package nostr.event.impl;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Data
@Builder
@Tag(code = "", nip = 12)
public class GenericTagQuery {
    
    private final Character tagName;
    private final List<String> value;
        
}
