package nostr.event;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    ZAP_REQUEST(9734, "zap_request"),
    ZAP_RECEIPT(9735, "zap_receipt"),
    REPLACEABLE_EVENT(10_000, "replaceable_event"),
    EPHEMEREAL_EVENT(20_000, "ephemereal_event"),
    CLIENT_AUTH(22_242, "authentication_of_clients_to_relays"),
    CLASSIFIED_LISTING(30_402, "classified_listing_active"),
    CLASSIFIED_LISTING_INACTIVE(30_403, "classified_listing_inactive"),
    CLASSIFIED_LISTING_DRAFT(30_403, "classified_listing_draft"),

    // NIP-60
    WALLET(37_375, "wallet"),
    WALLET_UNSPENT_PROOF(7_375, "wallet_unspent_proof"),
    WALLET_SPENDING_HISTORY(7_376, "wallet_spending_history"),
    
    // NIP-61
    NUTZAP_RECEIPT(7_376, "nutzap_receipt"),
    NUTZAP(9_321, "nutzap"),
    NUTZAP_INFO(10_019, "nutzap_info"),
    
    // Default
    UNDEFINED(-1, "undefined");

    @JsonValue
    private final int value;

    private final String name;

    @JsonCreator
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
