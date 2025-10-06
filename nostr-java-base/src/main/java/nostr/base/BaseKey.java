package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32EncodingException;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.util.NostrUtil;

/**
 * @author squirrel
 */
@AllArgsConstructor
@Data
@Slf4j
public abstract class BaseKey implements IKey {

  @NonNull @EqualsAndHashCode.Exclude protected final KeyType type;

  @NonNull protected final byte[] rawData;

  protected final Bech32Prefix prefix;

  @Override
  public String toBech32String() {
    try {
      return Bech32.toBech32(prefix, rawData);
    } catch (IllegalArgumentException ex) {
      log.error(
          "Invalid key data for Bech32 conversion for {} key with prefix {}", type, prefix, ex);
      throw new KeyEncodingException("Invalid key data for Bech32 conversion", ex);
    } catch (Bech32EncodingException ex) {
      log.error("Failed to convert {} key to Bech32 format with prefix {}", type, prefix, ex);
      throw new KeyEncodingException("Failed to convert key to Bech32", ex);
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
    if (this == o) return true;

    // null check
    if (o == null) return false;

    // type check and cast
    if (getClass() != o.getClass()) return false;

    BaseKey baseKey = (BaseKey) o;

    // field comparison
    return Arrays.equals(rawData, baseKey.rawData);
  }
}
