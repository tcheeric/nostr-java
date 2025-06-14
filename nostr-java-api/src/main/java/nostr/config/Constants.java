package nostr.config;

public interface Constants {

    interface Kind {
        int USER_METADATA = 0;
        int SHORT_TEXT_NOTE = 1;
        @Deprecated
        int RECOMMENDED_RELAY = 2;
        int CONTACT_LIST = 3;
        int ENCRYPTED_DIRECT_MESSAGE = 4;
        int EVENT_DELETION = 5;
        int OTS_ATTESTATION = 1040;
        int DATE_BASED_CALENDAR_CONTENT = 31922;
        int TIME_BASED_CALENDAR_CONTENT = 31923;
        int CALENDAR = 31924;
        int CALENDAR_EVENT_RSVP = 31925;
        int REPOST = 6;
        int REACTION = 7;
        int CHANNEL_CREATION = 40;
        int CHANNEL_METADATA = 41;
        int CHANNEL_MESSAGE = 42;
        int CHANNEL_HIDE_MESSAGE = 43;
        int CHANNEL_MUTE_USER = 44;
        int REPORT = 1984;
        int ZAP_REQUEST = 9734;
        int ZAP_RECEIPT = 9735;
        int RELAY_LIST_METADATA = 10002;
        int CLIENT_AUTHENTICATION = 22242;
        int BADGE_DEFINITION = 30008;
        int BADGE_AWARD = 30009;
        int LONG_FORM_TEXT_NOTE = 30023;
        int LONG_FORM_DRAFT = 30024;
        int APPLICATION_SPECIFIC_DATA = 30078;
        int CASHU_WALLET_EVENT = 17375;
        int CASHU_WALLET_TOKENS = 7375;
        int CASHU_WALLET_HISTORY = 7376;
        int CASHU_RESERVED_WALLET_TOKENS = 7374;
        int CASHU_NUTZAP_EVENT = 9321;
        int CASHU_NUTZAP_INFO_EVENT = 10019;
        int SET_STALL = 30017;
        int SET_PRODUCT = 30018;
        int REACTION_TO_WEBSITE = 17;
        int REQUEST_EVENTS = 24133;
        int CLASSIFIED_LISTING = 30_402;
        int RELAY_LIST_METADATA_EVENT = 10_002;
    }

    interface Tag {
        String EVENT_CODE = "e";
        String PUBKEY_CODE = "p";
        String IDENTITY_CODE = "d";
        String ADDRESS_CODE = "a";
        String HASHTAG_CODE = "t";
        String REFERENCE_CODE = "r";
        String GEOHASH_CODE = "g";
        String SUBJECT_CODE = "subject";
        String TITLE_CODE = "title";
        String IMAGE_CODE = "image";
        String PUBLISHED_AT_CODE = "published_at";
        String SUMMARY_CODE = "summary";
        String KIND_CODE = "k";
        String EMOJI_CODE = "emoji";
        String ALT_CODE = "alt";
        String NAMESPACE_CODE = "L";
        String LABEL_CODE = "l";
        String EXPIRATION_CODE = "expiration";
        String RELAY_CODE = "relay";
        String RELAYS_CODE = "relays";
        String CHALLENGE_CODE = "challenge";
        String AMOUNT_CODE = "amount";
        String LNURL_CODE = "lnurl";
        String BOLT11_CODE = "bolt11";
        String PREIMAGE_CODE = "preimage";
        String DESCRIPTION_CODE = "description";
        String ZAP_CODE = "zap";
        String RECIPIENT_PUBKEY_CODE = "P";
        String MINT_CODE = "mint";
        String UNIT_CODE = "unit";
        String PRIVKEY_CODE = "privkey";
        String BALANCE_CODE = "balance";
        String DIRECTION_CODE = "direction";
        String P2PKH_CODE = "pubkey";
        String URL_CODE = "u";
        String PROOF_CODE = "proof";
        String LOCATION_CODE = "location";
        String PRICE_CODE = "price";
        String STATUS_CODE = "status";
        String START_CODE = "start";
        String END_CODE = "end";
        String START_TZID_CODE = "start_tzid";
        String END_TZID_CODE = "end_tzid";
        String FREE_BUSY_CODE = "fb";
    }

    interface NIP {
        int ONE = 1;
        int TWO = 2;
    }
}