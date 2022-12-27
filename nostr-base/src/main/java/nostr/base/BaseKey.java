package nostr.base;

import nostr.util.NostrUtil;
import nostr.crypto.bech32.Bech32;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonString;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public abstract class BaseKey implements IKey {

    @NonNull
    @EqualsAndHashCode.Exclude
    protected final KeyType type;

    @NonNull
    @JsonString
    protected final byte[] rawData;

    protected final Bech32Prefix prefix;

    @Override
    public String toString() {
        return NostrUtil.bytesToHex(rawData);
    }

    @Override
    public String toBech32() {
        try {
            return Bech32.toBech32(prefix.getCode(), this.toString());
        } catch (NostrException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

}
