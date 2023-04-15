package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.annotation.JsonString;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseKey implements IKey {

    @NonNull
    @EqualsAndHashCode.Exclude
    protected final KeyType type;

    @NonNull
    @JsonString
    protected final byte[] rawData;

    protected final Bech32Prefix prefix;
    
    @Override
    public String getBech32() throws NostrException {
    	return Bech32.toBech32(prefix, rawData);
    }

    @JsonValue
    @Override
    public String toString() {
        return NostrUtil.bytesToHex(rawData);
    }

}
