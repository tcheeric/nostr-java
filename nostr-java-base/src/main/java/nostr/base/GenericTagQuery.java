
package nostr.base;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author squirrel
 */
@Data
@NoArgsConstructor
public class GenericTagQuery implements IElement {
    
    private Character tagName;
    private List<String> value;

    @Override
    public Integer getNip() {
        return 1;
    }        
}
