package nostr.base;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ElementAttribute {

    private final String name;
    private final Object value;
    private final Integer nip;

}
