package nostr.encryption;

public interface MessageCipher {

    String NIP_04 = "NIP04";
    String NIP_44 = "NIP44";

    String encrypt(String message);

    String decrypt(String message);
}
