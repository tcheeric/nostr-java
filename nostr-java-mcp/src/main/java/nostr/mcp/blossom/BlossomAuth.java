package nostr.mcp.blossom;

import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.mcp.tool.ToolFailure;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * The signed Nostr event a Blossom server accepts instead of a password.
 *
 * <p>BUD-11 authorizes every request with a kind-24242 event carrying the verb, an expiry and,
 * for the endpoints that act on one blob, its hash. The event is the credential, so it is built
 * as narrowly as the request needs: a token for uploading one blob cannot delete another, and it
 * stops working shortly after it is made.
 *
 * <p>The encoding is the part worth being exact about. The header is base64url without padding,
 * which is what BUD-11 specifies and what the reference client sends; standard base64 differs in
 * two characters and a server rejects it with a 401 that says nothing useful.
 */
public final class BlossomAuth {

  /** The event kind BUD-11 reserves for authorization. */
  public static final int AUTHORIZATION_KIND = 24_242;

  private static final String VERB_TAG = "t";
  private static final String EXPIRATION_TAG = "expiration";
  private static final String HASH_TAG = "x";
  private static final String NONCE_TAG = "nonce";
  private static final int NONCE_BYTES = 8;
  private static final String SCHEME = "Nostr ";

  /** How long a token stays valid. Long enough to upload a blob, short enough to be worthless if it leaks. */
  private static final long LIFETIME_SECONDS = 300;

  private final Clock clock;
  private final SecureRandom nonces = new SecureRandom();

  /**
   * @param clock what "now" means when stamping and expiring a token
   */
  public BlossomAuth(@NonNull Clock clock) {
    this.clock = clock;
  }

  /**
   * Build the unsigned token for a request.
   *
   * @param verb the BUD-11 action: get, upload, list or delete
   * @param blobHash the blob this token is limited to, or empty for a request about no one blob
   * @return the event to sign
   */
  public GenericEvent tokenFor(@NonNull BlossomVerb verb, @NonNull Optional<String> blobHash) {
    long now = clock.instant().getEpochSecond();
    List<BaseTag> tags = new ArrayList<>();
    tags.add(BaseTag.create(VERB_TAG, verb.wireName()));
    tags.add(BaseTag.create(EXPIRATION_TAG, Long.toString(now + LIFETIME_SECONDS)));
    blobHash.ifPresent(hash -> tags.add(BaseTag.create(HASH_TAG, hash)));
    tags.add(BaseTag.create(NONCE_TAG, newNonce()));
    return GenericEvent.builder()
        .kind(AUTHORIZATION_KIND)
        .content(verb.intent())
        .createdAt(now)
        .tags(tags)
        .build();
  }

  /**
   * A random value making every token a distinct event.
   *
   * <p>Without it, two tokens for the same verb and blob built in the same second are byte for
   * byte identical, and a nostr event's id is the hash of its contents — so the second request
   * carries an id the server has already seen. blossom-server keeps a replay cache and answers
   * the second one {@code 400 Auth event already used}, which is correct of it: a token that can
   * be presented twice is a token worth stealing.
   *
   * <p>It bites hardest where there is nothing else to vary. A list token carries no {@code x}
   * tag, so two listings in the same second would otherwise be the same event.
   */
  private String newNonce() {
    byte[] bytes = new byte[NONCE_BYTES];
    nonces.nextBytes(bytes);
    return HexFormat.of().formatHex(bytes);
  }

  /**
   * Render a signed token as the header value a server reads.
   *
   * @param signedToken the token, already signed
   * @return the {@code Authorization} value, scheme included
   * @throws nostr.mcp.tool.ToolException when the token cannot be serialised
   */
  public String headerValue(@NonNull GenericEvent signedToken) {
    if (!signedToken.isSigned()) {
      throw ToolFailure.INVALID_ARGUMENT.raise("Refusing to send an unsigned Blossom token");
    }
    String json = new BaseEventEncoder<>(signedToken).encode();
    return SCHEME
        + Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }
}
