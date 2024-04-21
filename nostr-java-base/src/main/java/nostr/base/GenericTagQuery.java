
package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    @JsonIgnore
    public Integer getNip() {
        return 1;
    }
}
