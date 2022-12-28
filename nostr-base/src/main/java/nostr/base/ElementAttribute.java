package nostr.base;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nostr.types.values.IValue;

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

    private final IValue value;
//    private final List valueList;
//        
//    @Builder.Default
//    private final boolean isString = Boolean.TRUE;
    private final Integer nip;
    
}
