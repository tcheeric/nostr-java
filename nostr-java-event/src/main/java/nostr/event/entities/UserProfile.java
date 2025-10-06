package nostr.event.entities;

import static nostr.base.json.EventJsonMapper.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import nostr.base.IBech32Encodable;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@Slf4j
public final class UserProfile extends Profile implements IBech32Encodable {

  @JsonIgnore private PublicKey publicKey;

  private String nip05;

  public UserProfile(
      @NonNull PublicKey publicKey, String name, String nip05, String about, URL picture) {
    super(name, about, picture);
    this.publicKey = publicKey;
    this.nip05 = nip05;
  }

  @Override
  public String toBech32() {
    try {
      return Bech32.encode(
          Bech32.Encoding.BECH32, Bech32Prefix.NPROFILE.getCode(), this.publicKey.getRawData());
    } catch (Exception ex) {
      log.error("Failed to convert UserProfile to Bech32 format", ex);
      throw new RuntimeException("Failed to convert UserProfile to Bech32 format", ex);
    }
  }

  @Override
  public String toString() {
    try {
      return mapper().writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
