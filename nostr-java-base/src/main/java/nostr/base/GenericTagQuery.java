
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
public class GenericTagQuery {
    
    private String tagName;
    private List<String> value;

    @JsonIgnore
    public Integer getNip() {
        return 1;
    }
}
