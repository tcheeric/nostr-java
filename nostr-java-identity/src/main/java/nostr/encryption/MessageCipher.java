package nostr.encryption;

public interface MessageCipher {

  /**
   * @deprecated NIP-04 leaves the correspondents and timing public. Prefer NIP-44, which
   *     {@link MessageCipher44} implements and which NIP-17 private messages build on.
   */
  @Deprecated(since = "2.1.0")
  String NIP_04 = "NIP04";
  String NIP_44 = "NIP44";

  String encrypt(String message);

  String decrypt(String message);
}
