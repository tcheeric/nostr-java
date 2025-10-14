package nostr.api.nip01;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.base.Marker;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.tag.IdentifierTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the canonical tags used by NIP-01 helpers.
 *
 * <p>These tags follow the standard defined in
 * <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a> and are used
 * throughout the API builders for consistency.
 */
public final class NIP01TagFactory {

  private NIP01TagFactory() {}

  public static BaseTag eventTag(@NonNull String relatedEventId) {
    return new BaseTagFactory(Constants.Tag.EVENT_CODE, List.of(relatedEventId)).create();
  }

  public static BaseTag eventTag(@NonNull String idEvent, String recommendedRelayUrl, Marker marker) {
    List<String> params = new ArrayList<>();
    params.add(idEvent);
    if (recommendedRelayUrl != null) {
      params.add(recommendedRelayUrl);
    }
    if (marker != null) {
      params.add(marker.getValue());
    }
    return new BaseTagFactory(Constants.Tag.EVENT_CODE, params).create();
  }

  public static BaseTag eventTag(@NonNull String idEvent, Marker marker) {
    return eventTag(idEvent, (String) null, marker);
  }

  public static BaseTag eventTag(@NonNull String idEvent, Relay recommendedRelay, Marker marker) {
    return eventTag(idEvent, recommendedRelay != null ? recommendedRelay.getUri() : null, marker);
  }

  public static BaseTag pubKeyTag(@NonNull PublicKey publicKey) {
    return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, List.of(publicKey.toString())).create();
  }

  public static BaseTag pubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
    List<String> params = new ArrayList<>();
    params.add(publicKey.toString());
    params.add(mainRelayUrl);
    params.add(petName);
    return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, params).create();
  }

  public static BaseTag pubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl) {
    List<String> params = new ArrayList<>();
    params.add(publicKey.toString());
    params.add(mainRelayUrl);
    return new BaseTagFactory(Constants.Tag.PUBKEY_CODE, params).create();
  }

  public static BaseTag identifierTag(@NonNull String id) {
    return new BaseTagFactory(Constants.Tag.IDENTITY_CODE, List.of(id)).create();
  }

  public static BaseTag addressTag(
      @NonNull Integer kind, @NonNull PublicKey publicKey, BaseTag idTag, Relay relay) {
    if (idTag != null && !(idTag instanceof IdentifierTag)) {
      throw new IllegalArgumentException("idTag must be an identifier tag");
    }

    List<String> params = new ArrayList<>();
    String param = kind + ":" + publicKey + ":";
    if (idTag instanceof IdentifierTag identifierTag) {
      String uuid = identifierTag.getUuid();
      if (uuid != null) {
        param += uuid;
      }
    }
    params.add(param);

    if (relay != null) {
      params.add(relay.getUri());
    }

    return new BaseTagFactory(Constants.Tag.ADDRESS_CODE, params).create();
  }

  public static BaseTag addressTag(
      @NonNull Integer kind, @NonNull PublicKey publicKey, String id, Relay relay) {
    return addressTag(kind, publicKey, identifierTag(id), relay);
  }

  public static BaseTag addressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, String id) {
    return addressTag(kind, publicKey, identifierTag(id), null);
  }
}
