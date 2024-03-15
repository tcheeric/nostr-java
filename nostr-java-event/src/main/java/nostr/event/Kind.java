package nostr.event;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Getter
public enum Kind {
  SET_METADATA(0, "set_metadata"),
  TEXT_NOTE(1, "text_note"),
  OTS_EVENT(1040, "ots_event"),
  RECOMMEND_SERVER(2, "recommend_server"),
  CONTACT_LIST(3, "contact_list"),
  ENCRYPTED_DIRECT_MESSAGE(4, "encrypted_direct_message"),
  ENCRYPTED_PAYLOADS(44, "encrypted_payloads"),
  DELETION(5, "deletion"),
  REPOST(6,"repost"),
  REACTION(7, "reaction"),
  CHANNEL_CREATE(40, "channel_create"),
  CHANNEL_METADATA(41, "channel_metadata"),
  CHANNEL_MESSAGE(42, "channel_message"),
  HIDE_MESSAGE(43, "hide_message"),
  MUTE_USER(44, "mute_user"),
  REPLACEABLE_EVENT(10_000, "replaceable_event"),
  EPHEMEREAL_EVENT(20_000, "ephemereal_event"),
  CLIENT_AUTH(22_242, "authentication_of_clients_to_relays"),
  KIND_SET_STALL(30_017, "set_stall"),
  KIND_SET_PRODUCT(30_018, "set_product"),
  CLASSIFIED_LISTING(30_402, "classified_listing"),
  UNDEFINED(-1, "undefined");

  @JsonValue
  private final int value;
  private final String name;

  public static Kind valueOf(int value) {
    for (Kind k : values()) {
      if (k.getValue() == value) {
        return k;
      }
    }

    return UNDEFINED;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }
}
