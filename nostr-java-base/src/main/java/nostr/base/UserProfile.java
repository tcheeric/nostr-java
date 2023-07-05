package nostr.base;

import java.net.URL;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;

import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Log
public final class UserProfile extends Profile implements IBech32Encodable {

    private final PublicKey publicKey;
    private final String nip05;

    public UserProfile(@NonNull PublicKey publicKey, String name, String nip05, String about, URL picture) {
        super(name, about, picture);
        this.publicKey = publicKey;
        this.nip05 = nip05;
    }

    @Override
    public String toBech32() {
        try {
            return Bech32.encode(Bech32.Encoding.BECH32, Bech32Prefix.NPROFILE.getCode(), this.publicKey.getRawData());
        } catch (NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
