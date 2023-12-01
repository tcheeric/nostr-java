package nostr.crypto.bech32;

import lombok.Getter;

/**
 *
 * @author squirrel
 */
@Getter
public enum Bech32Prefix {
    NPUB("npub", "public keys"),
    NSEC("nsec", "private keys"),
    NOTE("note", "note ids"),
    NPROFILE("nprofile", "nostr profile"),
    NEVENT("nevent", "nostr event");

    private final String code;
    private final String description;

    Bech32Prefix(String code, String description) {
        this.code = code;
        this.description = description;
    }


}
