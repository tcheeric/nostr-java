package nostr.base;

import java.time.temporal.ValueRange;

/**
 * Constants and utility methods for Nostr event kinds.
 *
 * <p>This replaces the Kind enum with simple int constants and static range-check methods.
 */
public final class Kinds {

  private Kinds() {}

  // Standard event kinds
  public static final int SET_METADATA = 0;
  public static final int TEXT_NOTE = 1;
  public static final int RECOMMEND_SERVER = 2;
  public static final int CONTACT_LIST = 3;
  public static final int ENCRYPTED_DIRECT_MESSAGE = 4;
  public static final int DELETION = 5;
  public static final int REPOST = 6;
  public static final int REACTION = 7;
  public static final int REACTION_TO_WEBSITE = 17;
  public static final int CHANNEL_CREATE = 40;
  public static final int CHANNEL_METADATA = 41;
  public static final int CHANNEL_MESSAGE = 42;
  public static final int HIDE_MESSAGE = 43;
  public static final int MUTE_USER = 44;
  public static final int OTS_EVENT = 1040;
  public static final int REPORT = 1984;
  public static final int COINJOIN_POOL = 2022;
  public static final int RESERVED_CASHU_WALLET_TOKENS = 7_374;
  public static final int WALLET_UNSPENT_PROOF = 7_375;
  public static final int WALLET_TX_HISTORY = 7_376;
  public static final int NUTZAP = 9_321;
  public static final int ZAP_REQUEST = 9_734;
  public static final int ZAP_RECEIPT = 9_735;
  public static final int REPLACEABLE_EVENT = 10_000;
  public static final int PIN_LIST = 10_001;
  public static final int RELAY_LIST_METADATA = 10_002;
  public static final int NUTZAP_INFORMATIONAL = 10_019;
  public static final int WALLET = 17_375;
  public static final int EPHEMERAL_EVENT = 20_000;
  public static final int CLIENT_AUTH = 22_242;
  public static final int NOSTR_CONNECT = 24_133;
  public static final int ADDRESSABLE_EVENT = 30_000;
  public static final int BADGE_DEFINITION = 30_008;
  public static final int BADGE_AWARD = 30_009;
  public static final int STALL_CREATE_OR_UPDATE = 30_017;
  public static final int PRODUCT_CREATE_OR_UPDATE = 30_018;
  public static final int LONG_FORM_TEXT_NOTE = 30_023;
  public static final int LONG_FORM_DRAFT = 30_024;
  public static final int APPLICATION_SPECIFIC_DATA = 30_078;
  public static final int CLASSIFIED_LISTING = 30_402;
  public static final int CLASSIFIED_LISTING_INACTIVE = 30_403;
  public static final int CALENDAR_DATE_BASED_EVENT = 31_922;
  public static final int CALENDAR_TIME_BASED_EVENT = 31_923;
  public static final int CALENDAR_EVENT = 31_924;
  public static final int CALENDAR_RSVP_EVENT = 31_925;

  public static boolean isValid(int kind) {
    return ValueRange.of(0, 65_535).isValidIntValue(kind);
  }

  public static boolean isReplaceable(int kind) {
    return kind >= 10_000 && kind < 20_000;
  }

  public static boolean isEphemeral(int kind) {
    return kind >= 20_000 && kind < 30_000;
  }

  public static boolean isAddressable(int kind) {
    return kind >= 30_000 && kind < 40_000;
  }
}
