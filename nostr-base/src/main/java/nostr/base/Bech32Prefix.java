package nostr.base;

/**
 *
 * @author squirrel
 */
public enum Bech32Prefix {
    NPUB("npub", "public keys"),
    NSEC("nsec", "private keys"),
    NOTE("note", "note ids"),
    NPROFILE("nprofile", "nostr profile"),
    NEVENT("nevent", "nostr event");

    private final String code;
    private final String description;

    private Bech32Prefix(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    
    
}
