package nostr.config;

/** Collection of common constants used across the API. */
public final class Constants {
  private Constants() {}

  // Deprecated Constants.Kind facade removed in 1.0.0. Use nostr.base.Kind instead.

  public static final class Tag {
    private Tag() {}

    public static final String EVENT_CODE = "e";
    public static final String PUBKEY_CODE = "p";
    public static final String IDENTITY_CODE = "d";
    public static final String ADDRESS_CODE = "a";
    public static final String HASHTAG_CODE = "t";
    public static final String REFERENCE_CODE = "r";
    public static final String GEOHASH_CODE = "g";
    public static final String SUBJECT_CODE = "subject";
    public static final String TITLE_CODE = "title";
    public static final String IMAGE_CODE = "image";
    public static final String PUBLISHED_AT_CODE = "published_at";
    public static final String SUMMARY_CODE = "summary";
    public static final String KIND_CODE = "k";
    public static final String EMOJI_CODE = "emoji";
    public static final String ALT_CODE = "alt";
    public static final String NAMESPACE_CODE = "L";
    public static final String LABEL_CODE = "l";
    public static final String EXPIRATION_CODE = "expiration";
    public static final String RELAY_CODE = "relay";
    public static final String RELAYS_CODE = "relays";
    public static final String CHALLENGE_CODE = "challenge";
    public static final String AMOUNT_CODE = "amount";
    public static final String LNURL_CODE = "lnurl";
    public static final String BOLT11_CODE = "bolt11";
    public static final String PREIMAGE_CODE = "preimage";
    public static final String DESCRIPTION_CODE = "description";
    public static final String DESCRIPTION_HASH_CODE = "description_hash";
    public static final String ZAP_CODE = "zap";
    public static final String RECIPIENT_PUBKEY_CODE = "P";
    public static final String MINT_CODE = "mint";
    public static final String UNIT_CODE = "unit";
    public static final String PRIVKEY_CODE = "privkey";
    public static final String BALANCE_CODE = "balance";
    public static final String DIRECTION_CODE = "direction";
    public static final String P2PKH_CODE = "pubkey";
    public static final String URL_CODE = "u";
    public static final String PROOF_CODE = "proof";
    public static final String LOCATION_CODE = "location";
    public static final String PRICE_CODE = "price";
    public static final String STATUS_CODE = "status";
    public static final String START_CODE = "start";
    public static final String END_CODE = "end";
    public static final String START_TZID_CODE = "start_tzid";
    public static final String END_TZID_CODE = "end_tzid";
    public static final String FREE_BUSY_CODE = "fb";
  }
}

