package nostr.encryption;

public interface MessageCipher {

    public static final String NIP_04 = "NIP04";
    public static final String NIP_44 = "NIP44";

    public String encrypt(String message);

    public String decrypt(String message);
}
