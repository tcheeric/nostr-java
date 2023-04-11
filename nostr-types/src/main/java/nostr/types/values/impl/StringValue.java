package nostr.types.values.impl;

import nostr.types.Type;
import nostr.types.values.BaseValue;

/**
 *
 * @author squirrel
 */
public class StringValue extends BaseValue {

    public StringValue(String value) {
        super(Type.STRING, value);
    }
}
