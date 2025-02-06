
package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author squirrel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericTagQuery {

    private String tagName;
    private List<String> value;

    @JsonIgnore
    public Integer getNip() {
        return 1;
    }
}
