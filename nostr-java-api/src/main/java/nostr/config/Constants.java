package nostr.config;

import nostr.base.Kind;

/** Collection of common constants used across the API. */
public final class Constants {
  private Constants() {}

  /**
   * @deprecated Prefer using {@link Kind} directly. This indirection remains for backward
   *     compatibility and will be removed in a future release.
   */
  @Deprecated(forRemoval = true, since = "1.2.0")
  public static final class Kind {
    private Kind() {}

    public static final int USER_METADATA = Kind.SET_METADATA.getValue();
    public static final int SHORT_TEXT_NOTE = Kind.TEXT_NOTE.getValue();
    /** @deprecated Use {@link Kind#RECOMMEND_SERVER}. */
    @Deprecated public static final int RECOMMENDED_RELAY = Kind.RECOMMEND_SERVER.getValue();
    public static final int CONTACT_LIST = Kind.CONTACT_LIST.getValue();
    public static final int ENCRYPTED_DIRECT_MESSAGE = Kind.ENCRYPTED_DIRECT_MESSAGE.getValue();
    public static final int EVENT_DELETION = Kind.DELETION.getValue();
    public static final int OTS_ATTESTATION = Kind.OTS_EVENT.getValue();
    public static final int DATE_BASED_CALENDAR_CONTENT = Kind.CALENDAR_DATE_BASED_EVENT.getValue();
    public static final int TIME_BASED_CALENDAR_CONTENT = Kind.CALENDAR_TIME_BASED_EVENT.getValue();
    public static final int CALENDAR = Kind.CALENDAR_EVENT.getValue();
    public static final int CALENDAR_EVENT_RSVP = Kind.CALENDAR_RSVP_EVENT.getValue();
    public static final int REPOST = Kind.REPOST.getValue();
    public static final int REACTION = Kind.REACTION.getValue();
    public static final int CHANNEL_CREATION = Kind.CHANNEL_CREATE.getValue();
    public static final int CHANNEL_METADATA = Kind.CHANNEL_METADATA.getValue();
    public static final int CHANNEL_MESSAGE = Kind.CHANNEL_MESSAGE.getValue();
    public static final int CHANNEL_HIDE_MESSAGE = Kind.HIDE_MESSAGE.getValue();
    public static final int CHANNEL_MUTE_USER = Kind.MUTE_USER.getValue();
    public static final int REPORT = Kind.REPORT.getValue();
    public static final int ZAP_REQUEST = Kind.ZAP_REQUEST.getValue();
    public static final int ZAP_RECEIPT = Kind.ZAP_RECEIPT.getValue();
    public static final int RELAY_LIST_METADATA = Kind.RELAY_LIST_METADATA.getValue();
    public static final int CLIENT_AUTHENTICATION = Kind.CLIENT_AUTH.getValue();
    public static final int BADGE_DEFINITION = Kind.BADGE_DEFINITION.getValue();
    public static final int BADGE_AWARD = Kind.BADGE_AWARD.getValue();
    public static final int LONG_FORM_TEXT_NOTE = Kind.LONG_FORM_TEXT_NOTE.getValue();
    public static final int LONG_FORM_DRAFT = Kind.LONG_FORM_DRAFT.getValue();
    public static final int APPLICATION_SPECIFIC_DATA = Kind.APPLICATION_SPECIFIC_DATA.getValue();
    public static final int CASHU_WALLET_EVENT = Kind.WALLET.getValue();
    public static final int CASHU_WALLET_TOKENS = Kind.WALLET_UNSPENT_PROOF.getValue();
    public static final int CASHU_WALLET_HISTORY = Kind.WALLET_TX_HISTORY.getValue();
    public static final int CASHU_RESERVED_WALLET_TOKENS = Kind.RESERVED_CASHU_WALLET_TOKENS.getValue();
    public static final int CASHU_NUTZAP_EVENT = Kind.NUTZAP.getValue();
    public static final int CASHU_NUTZAP_INFO_EVENT = Kind.NUTZAP_INFORMATIONAL.getValue();
    public static final int SET_STALL = Kind.STALL_CREATE_OR_UPDATE.getValue();
    public static final int SET_PRODUCT = Kind.PRODUCT_CREATE_OR_UPDATE.getValue();
    public static final int REACTION_TO_WEBSITE = Kind.REACTION_TO_WEBSITE.getValue();
    public static final int REQUEST_EVENTS = Kind.REQUEST_EVENTS.getValue();
    public static final int CLASSIFIED_LISTING = Kind.CLASSIFIED_LISTING.getValue();
    public static final int RELAY_LIST_METADATA_EVENT = Kind.RELAY_LIST_METADATA.getValue();
  }

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
