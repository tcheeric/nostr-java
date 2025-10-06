package nostr.config;

import nostr.base.Kind;

/** Collection of common constants used across the API. */
public final class Constants {
  private Constants() {}

  /**
   * @deprecated Use {@link nostr.base.Kind} enum directly instead. This class provides integer
   *     constants for backward compatibility only and will be removed in version 1.0.0.
   *
   *     <p>Migration guide:
   *     <pre>{@code
   *     // Old (deprecated):
   *     new GenericEvent(pubKey, Constants.Kind.USER_METADATA);
   *
   *     // New (recommended):
   *     new GenericEvent(pubKey, Kind.SET_METADATA);
   *     // or use the integer value directly:
   *     new GenericEvent(pubKey, Kind.SET_METADATA.getValue());
   *     }</pre>
   *
   * @see nostr.base.Kind
   */
  @Deprecated(forRemoval = true, since = "0.6.2")
  public static final class Kind {
    private Kind() {}

    /** @deprecated Use {@link nostr.base.Kind#SET_METADATA} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int USER_METADATA = nostr.base.Kind.SET_METADATA.getValue();

    /** @deprecated Use {@link nostr.base.Kind#TEXT_NOTE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int SHORT_TEXT_NOTE = nostr.base.Kind.TEXT_NOTE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#RECOMMEND_SERVER} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int RECOMMENDED_RELAY = nostr.base.Kind.RECOMMEND_SERVER.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CONTACT_LIST} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CONTACT_LIST = nostr.base.Kind.CONTACT_LIST.getValue();

    /** @deprecated Use {@link nostr.base.Kind#ENCRYPTED_DIRECT_MESSAGE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int ENCRYPTED_DIRECT_MESSAGE =
        nostr.base.Kind.ENCRYPTED_DIRECT_MESSAGE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#DELETION} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int EVENT_DELETION = nostr.base.Kind.DELETION.getValue();

    /** @deprecated Use {@link nostr.base.Kind#REPOST} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int REPOST = nostr.base.Kind.REPOST.getValue();

    /** @deprecated Use {@link nostr.base.Kind#REACTION} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int REACTION = nostr.base.Kind.REACTION.getValue();

    /** @deprecated Use {@link nostr.base.Kind#REACTION_TO_WEBSITE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int REACTION_TO_WEBSITE = nostr.base.Kind.REACTION_TO_WEBSITE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CHANNEL_CREATE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CHANNEL_CREATION = nostr.base.Kind.CHANNEL_CREATE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CHANNEL_METADATA} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CHANNEL_METADATA = nostr.base.Kind.CHANNEL_METADATA.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CHANNEL_MESSAGE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CHANNEL_MESSAGE = nostr.base.Kind.CHANNEL_MESSAGE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#HIDE_MESSAGE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CHANNEL_HIDE_MESSAGE = nostr.base.Kind.HIDE_MESSAGE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#MUTE_USER} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CHANNEL_MUTE_USER = nostr.base.Kind.MUTE_USER.getValue();

    /** @deprecated Use {@link nostr.base.Kind#OTS_EVENT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int OTS_ATTESTATION = nostr.base.Kind.OTS_EVENT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#REPORT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int REPORT = nostr.base.Kind.REPORT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#ZAP_REQUEST} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int ZAP_REQUEST = nostr.base.Kind.ZAP_REQUEST.getValue();

    /** @deprecated Use {@link nostr.base.Kind#ZAP_RECEIPT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int ZAP_RECEIPT = nostr.base.Kind.ZAP_RECEIPT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#RELAY_LIST_METADATA} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int RELAY_LIST_METADATA = nostr.base.Kind.RELAY_LIST_METADATA.getValue();

    /** @deprecated Duplicate of RELAY_LIST_METADATA. Use {@link nostr.base.Kind#RELAY_LIST_METADATA} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int RELAY_LIST_METADATA_EVENT = nostr.base.Kind.RELAY_LIST_METADATA.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CLIENT_AUTH} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CLIENT_AUTHENTICATION = nostr.base.Kind.CLIENT_AUTH.getValue();

    /** @deprecated Use {@link nostr.base.Kind#REQUEST_EVENTS} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int REQUEST_EVENTS = nostr.base.Kind.REQUEST_EVENTS.getValue();

    /** @deprecated Use {@link nostr.base.Kind#BADGE_DEFINITION} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int BADGE_DEFINITION = nostr.base.Kind.BADGE_DEFINITION.getValue();

    /** @deprecated Use {@link nostr.base.Kind#BADGE_AWARD} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int BADGE_AWARD = nostr.base.Kind.BADGE_AWARD.getValue();

    /** @deprecated Use {@link nostr.base.Kind#STALL_CREATE_OR_UPDATE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int SET_STALL = nostr.base.Kind.STALL_CREATE_OR_UPDATE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#PRODUCT_CREATE_OR_UPDATE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int SET_PRODUCT = nostr.base.Kind.PRODUCT_CREATE_OR_UPDATE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#LONG_FORM_TEXT_NOTE} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int LONG_FORM_TEXT_NOTE = nostr.base.Kind.LONG_FORM_TEXT_NOTE.getValue();

    /** @deprecated Use {@link nostr.base.Kind#LONG_FORM_DRAFT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int LONG_FORM_DRAFT = nostr.base.Kind.LONG_FORM_DRAFT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#APPLICATION_SPECIFIC_DATA} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int APPLICATION_SPECIFIC_DATA =
        nostr.base.Kind.APPLICATION_SPECIFIC_DATA.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CLASSIFIED_LISTING} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CLASSIFIED_LISTING = nostr.base.Kind.CLASSIFIED_LISTING.getValue();

    /** @deprecated Use {@link nostr.base.Kind#WALLET} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_WALLET_EVENT = nostr.base.Kind.WALLET.getValue();

    /** @deprecated Use {@link nostr.base.Kind#WALLET_UNSPENT_PROOF} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_WALLET_TOKENS = nostr.base.Kind.WALLET_UNSPENT_PROOF.getValue();

    /** @deprecated Use {@link nostr.base.Kind#WALLET_TX_HISTORY} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_WALLET_HISTORY = nostr.base.Kind.WALLET_TX_HISTORY.getValue();

    /** @deprecated Use {@link nostr.base.Kind#RESERVED_CASHU_WALLET_TOKENS} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_RESERVED_WALLET_TOKENS =
        nostr.base.Kind.RESERVED_CASHU_WALLET_TOKENS.getValue();

    /** @deprecated Use {@link nostr.base.Kind#NUTZAP} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_NUTZAP_EVENT = nostr.base.Kind.NUTZAP.getValue();

    /** @deprecated Use {@link nostr.base.Kind#NUTZAP_INFORMATIONAL} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CASHU_NUTZAP_INFO_EVENT = nostr.base.Kind.NUTZAP_INFORMATIONAL.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CALENDAR_DATE_BASED_EVENT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int DATE_BASED_CALENDAR_CONTENT =
        nostr.base.Kind.CALENDAR_DATE_BASED_EVENT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CALENDAR_TIME_BASED_EVENT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int TIME_BASED_CALENDAR_CONTENT =
        nostr.base.Kind.CALENDAR_TIME_BASED_EVENT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CALENDAR_EVENT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CALENDAR = nostr.base.Kind.CALENDAR_EVENT.getValue();

    /** @deprecated Use {@link nostr.base.Kind#CALENDAR_RSVP_EVENT} instead */
    @Deprecated(forRemoval = true, since = "0.6.2")
    public static final int CALENDAR_EVENT_RSVP = nostr.base.Kind.CALENDAR_RSVP_EVENT.getValue();
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
