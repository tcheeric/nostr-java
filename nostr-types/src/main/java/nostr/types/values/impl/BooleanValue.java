
package nostr.types.values.impl;

import nostr.types.Type;
import nostr.types.values.BaseValue;

/**
 *
 * @author squirrel
 */
public class BooleanValue  extends BaseValue {
    
    public BooleanValue(Boolean value) {
        super(Type.BOOLEAN, value);
    }
}
