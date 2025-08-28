package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrUtil;

/**
 * @author squirrel
 */
@Data
@NoArgsConstructor
@Slf4j
public class Signature {

  @JsonProperty("rawData")
  private byte[] rawData;

  @JsonIgnore private PublicKey pubKey;

  @JsonValue
  @Override
  public String toString() {
    return NostrUtil.bytesToHex(rawData);
  }

  public static Signature fromString(String sig) {
    log.debug("Creating signature from string");
    Signature signature = new Signature();
    signature.setRawData(NostrUtil.hex128ToBytes(sig));
    return signature;
  }
}
