package nostr.mcp.blossom;

/**
 * The actions BUD-11 lets a token authorize.
 *
 * <p>A server checks the verb against the endpoint it was sent to, so the verb is what stops an
 * upload token from deleting anything. Each carries the sentence a person would be shown when
 * asked to approve it, which BUD-11 requires the content to be.
 */
public enum BlossomVerb {
  /** Read one blob. */
  GET("get", "Download blob"),
  /** Store one blob. */
  UPLOAD("upload", "Upload blob"),
  /** List the blobs a public key has stored. */
  LIST("list", "List blobs"),
  /** Remove one blob. */
  DELETE("delete", "Delete blob");

  private final String wireName;
  private final String intent;

  BlossomVerb(String wireName, String intent) {
    this.wireName = wireName;
    this.intent = intent;
  }

  /**
   * The value the {@code t} tag carries.
   *
   * @return the verb as BUD-11 spells it
   */
  public String wireName() {
    return wireName;
  }

  /**
   * What this token is for, in words a person could approve.
   *
   * @return the event content
   */
  public String intent() {
    return intent;
  }
}
