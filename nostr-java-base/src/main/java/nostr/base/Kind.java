package nostr.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.temporal.ValueRange;

/**
 * @author squirrel
 */
@AllArgsConstructor
@Getter
public enum Kind {
  SET_METADATA(0, "set_metadata"),
  TEXT_NOTE(1, "text_note"),
  RECOMMEND_SERVER(2, "recommend_server"),
  COINJOIN_POOL(2022, "coinjoin_pool"),
  REACTION_TO_WEBSITE(17, "reaction_to_website"),
  CONTACT_LIST(3, "contact_list"),
  ENCRYPTED_DIRECT_MESSAGE(4, "encrypted_direct_message"),
  DELETION(5, "deletion"),
  REPOST(6, "repost"),
  REACTION(7, "reaction"),
  REPORT(1984, "report"),
  CHANNEL_CREATE(40, "channel_create"),
  CHANNEL_METADATA(41, "channel_metadata"),
  CHANNEL_MESSAGE(42, "channel_message"),
  HIDE_MESSAGE(43, "hide_message"),
  MUTE_USER(44, "mute_user"),
  OTS_EVENT(1040, "ots_event"),
  RESERVED_CASHU_WALLET_TOKENS(7_374, "reserved_cashu_wallet_tokens"),
  WALLET(17_375, "wallet"),
  WALLET_UNSPENT_PROOF(7_375, "wallet_unspent_proof"),
  WALLET_TX_HISTORY(7_376, "wallet_tx_history"),
  ZAP_REQUEST(9734, "zap_request"),
  ZAP_RECEIPT(9735, "zap_receipt"),
  BADGE_DEFINITION(30_008, "badge_definition"),
  BADGE_AWARD(30_009, "badge_award"),
  REPLACEABLE_EVENT(10_000, "replaceable_event"),
  EPHEMEREAL_EVENT(20_000, "ephemereal_event"),
  ADDRESSABLE_EVENT(30_000, "addressable_event"),
  PIN_LIST(10_001, "pin_list"),
  CLIENT_AUTH(22_242, "authentication_of_clients_to_relays"),
  STALL_CREATE_OR_UPDATE(30_017, "create_or_update_stall"),
  PRODUCT_CREATE_OR_UPDATE(30_018, "create_or_update_product"),
  LONG_FORM_TEXT_NOTE(30_023, "long_form_text_note"),
  LONG_FORM_DRAFT(30_024, "long_form_draft"),
  APPLICATION_SPECIFIC_DATA(30_078, "application_specific_data"),
  CLASSIFIED_LISTING(30_402, "classified_listing_active"),
  CLASSIFIED_LISTING_INACTIVE(30_403, "classified_listing_inactive"),
  CLASSIFIED_LISTING_DRAFT(30_403, "classified_listing_draft"),
  CALENDAR_DATE_BASED_EVENT(31_922, "calendar_date_based_event"),
  CALENDAR_TIME_BASED_EVENT(31_923, "calendar_time_based_event"),
  CALENDAR_EVENT(31_924, "calendar_event"),
  CALENDAR_RSVP_EVENT(31_925, "calendar_rsvp_event"),
  NUTZAP_INFORMATIONAL(10_019, "nutzap_informational"),
  NUTZAP(9_321, "nutzap"),
  RELAY_LIST_METADATA(10_002, "relay_list_metadata"),
  NOSTR_CONNECT(24_133, "nostr_connect");

  @JsonValue private final int value;

  private final String name;

  @JsonCreator
  public static Kind valueOf(int value) {
    if (!ValueRange.of(0, 65_535).isValidIntValue(value)) {
      throw new IllegalArgumentException(
          String.format("Kind must be between 0 and 65535 but was [%d]", value));
    }
    for (Kind k : values()) {
      if (k.getValue() == value) {
        return k;
      }
    }

    throw new IllegalArgumentException(
        String.format("Unknown kind value: %d. Add it to the Kind enum if it's a valid NIP kind.", value));
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
