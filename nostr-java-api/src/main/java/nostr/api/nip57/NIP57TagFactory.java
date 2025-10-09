package nostr.api.nip57;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 * Centralizes construction of NIP-57 related tags.
 */
public final class NIP57TagFactory {

  private NIP57TagFactory() {}

  public static BaseTag lnurl(@NonNull String lnurl) {
    return new BaseTagFactory(Constants.Tag.LNURL_CODE, lnurl).create();
  }

  public static BaseTag bolt11(@NonNull String bolt11) {
    return new BaseTagFactory(Constants.Tag.BOLT11_CODE, bolt11).create();
  }

  public static BaseTag preimage(@NonNull String preimage) {
    return new BaseTagFactory(Constants.Tag.PREIMAGE_CODE, preimage).create();
  }

  public static BaseTag description(@NonNull String description) {
    return new BaseTagFactory(Constants.Tag.DESCRIPTION_CODE, description).create();
  }

  public static BaseTag descriptionHash(@NonNull String descriptionHashHex) {
    return new BaseTagFactory(Constants.Tag.DESCRIPTION_HASH_CODE, descriptionHashHex).create();
  }

  public static BaseTag amount(@NonNull Number amount) {
    return new BaseTagFactory(Constants.Tag.AMOUNT_CODE, amount.toString()).create();
  }

  public static BaseTag zapSender(@NonNull PublicKey publicKey) {
    return new BaseTagFactory(Constants.Tag.RECIPIENT_PUBKEY_CODE, publicKey.toString()).create();
  }

  public static BaseTag zap(
      @NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    List<String> params = new ArrayList<>();
    params.add(receiver.toString());
    relays.stream().map(Relay::getUri).forEach(params::add);
    if (weight != null) {
      params.add(weight.toString());
    }
    return BaseTag.create(Constants.Tag.ZAP_CODE, params);
  }

  public static BaseTag zap(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    return zap(receiver, relays, null);
  }
}
