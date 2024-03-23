package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

import java.util.Arrays;

/**
 * @author squirrel
 */
@AllArgsConstructor
@Data
public abstract class BaseKey implements IKey {

    @NonNull
    @EqualsAndHashCode.Exclude
    protected final KeyType type;

    @NonNull
    protected final byte[] rawData;

    protected final Bech32Prefix prefix;

    @Override
    public String toBech32String() {
        try {
            return Bech32.toBech32(prefix, rawData);
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @JsonValue
    public String toString() {
        return toHexString();
    }

    public String toHexString() {
        return NostrUtil.bytesToHex(rawData);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.type.hashCode();
        hash = 31 * hash + (this.prefix == null ? 0 : this.prefix.hashCode());
        hash = 31 * hash + (this.rawData == null ? 0 : Arrays.hashCode(this.rawData));
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        // null check
        if (o == null)
            return false;

        // type check and cast
        if (getClass() != o.getClass())
            return false;

        BaseKey baseKey = (BaseKey) o;

        // field comparison
        return Arrays.equals(rawData, baseKey.rawData);
    }
}
