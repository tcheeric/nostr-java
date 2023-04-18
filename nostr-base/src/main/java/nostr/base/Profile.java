package nostr.base;

import nostr.crypto.bech32.Bech32;
import java.net.URL;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode
@Log
public final class Profile implements IBech32Encodable {

    private final String name;

    @ToString.Exclude
    private final PublicKey publicKey;

    private String about;

    @ToString.Exclude
    private URL picture;

    private String nip05;

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
