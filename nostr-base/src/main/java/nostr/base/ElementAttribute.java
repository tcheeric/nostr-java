package nostr.base;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode
public class ElementAttribute {

    private final String name;
    
    private final List valueList;
    
    @Builder.Default
    private final boolean isString = Boolean.TRUE;
    
    private final Integer nip;

}
