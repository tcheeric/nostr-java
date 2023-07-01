package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nostr.util.NostrUtil;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author squirrel
 */
@Data
@NoArgsConstructor
public class Signature {
    
    @JsonProperty("rawData")
    private byte[] rawData;

    @JsonIgnore
    private PublicKey pubKey;

    @JsonValue
    @Override
    public String toString() {
        return NostrUtil.bytesToHex(rawData);
    }
}
