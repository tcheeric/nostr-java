
package nostr.config;

/**
 * Collection of common constants used across the API.
 */
public final class Constants {

    public static final class Kind {
        private Kind() {
        }

        public static final int USER_METADATA = 0;
        public static final int SHORT_TEXT_NOTE = 1;
        @Deprecated
        public static final int RECOMMENDED_RELAY = 2;
        public static final int CONTACT_LIST = 3;
        public static final int ENCRYPTED_DIRECT_MESSAGE = 4;
        public static final int EVENT_DELETION = 5;
        public static final int OTS_ATTESTATION = 1040;
        public static final int DATE_BASED_CALENDAR_CONTENT = 31922;
        public static final int TIME_BASED_CALENDAR_CONTENT = 31923;
        public static final int CALENDAR = 31924;
        public static final int CALENDAR_EVENT_RSVP = 31925;
        public static final int REPOST = 6;
        public static final int REACTION = 7;
        public static final int CHANNEL_CREATION = 40;
        public static final int CHANNEL_METADATA = 41;
        public static final int CHANNEL_MESSAGE = 42;
        public static final int CHANNEL_HIDE_MESSAGE = 43;
        public static final int CHANNEL_MUTE_USER = 44;
        public static final int REPORT = 1984;
        public static final int ZAP_REQUEST = 9734;
        public static final int ZAP_RECEIPT = 9735;
        public static final int RELAY_LIST_METADATA = 10002;
        public static final int CLIENT_AUTHENTICATION = 22242;
        public static final int BADGE_DEFINITION = 30008;
        public static final int BADGE_AWARD = 30009;
        public static final int LONG_FORM_TEXT_NOTE = 30023;
        public static final int LONG_FORM_DRAFT = 30024;
        public static final int APPLICATION_SPECIFIC_DATA = 30078;
        public static final int CASHU_WALLET_EVENT = 17375;
        public static final int CASHU_WALLET_TOKENS = 7375;
        public static final int CASHU_WALLET_HISTORY = 7376;
        public static final int CASHU_RESERVED_WALLET_TOKENS = 7374;
        public static final int CASHU_NUTZAP_EVENT = 9321;
        public static final int CASHU_NUTZAP_INFO_EVENT = 10019;
        public static final int SET_STALL = 30017;
        public static final int SET_PRODUCT = 30018;
        public static final int REACTION_TO_WEBSITE = 17;
        public static final int REQUEST_EVENTS = 24133;
        public static final int CLASSIFIED_LISTING = 30_402;
        public static final int RELAY_LIST_METADATA_EVENT = 10_002;
    }

    public static final class Tag {
        private Tag() {
        }

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

    interface NIP {
        int ONE = 1;
        int TWO = 2;
    }
}