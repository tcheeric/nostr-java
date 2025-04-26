package nostr.event.entities;

import java.net.URL;
import java.util.logging.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.java.Log;
import nostr.base.IBech32Encodable;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Log
public final class UserProfile extends Profile implements IBech32Encodable {

    @JsonIgnore
    private PublicKey publicKey;

    private String nip05;

    public UserProfile(@NonNull PublicKey publicKey, String name, String nip05, String about, URL picture) {
        super(name, about, picture);
        this.publicKey = publicKey;
        this.nip05 = nip05;
    }

    @Override
    public String toBech32() {
        try {
            return Bech32.encode(Bech32.Encoding.BECH32, Bech32Prefix.NPROFILE.getCode(), this.publicKey.getRawData());
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        try {
            return MAPPER_AFTERBURNER.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
