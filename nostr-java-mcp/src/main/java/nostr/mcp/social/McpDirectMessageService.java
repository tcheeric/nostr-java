package nostr.mcp.social;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.DirectMessagePublisher;
import nostr.api.RecipientDeliveryOutcome;
import nostr.api.RelayListLookup;
import nostr.base.PublicKey;
import nostr.client.relay.RelayPool;
import nostr.encryption.Nip17DirectMessageService;
import nostr.event.impl.ChatMessage;
import nostr.event.impl.GenericEvent;
import nostr.mcp.identity.IdentityVault;
import nostr.mcp.tool.ToolFailure;

import java.util.List;
import java.util.Set;

/**
 * Sends and reads private messages without the tool layer ever holding a key.
 *
 * <p>NIP-17 sealing derives shared secrets, so the SDK's service needs an {@link
 * nostr.id.Identity} rather than a signature. Building it inside the vault keeps the boundary
 * intact: this class receives something that can compose and read messages, and never the
 * identity that does so.
 *
 * <p>Decryption is opt-in per identity. Reading someone's private correspondence puts it into
 * the model's context and therefore into the host's logs and probably a third-party API, which
 * is a decision for the person whose messages they are rather than a default.
 */
@Slf4j
public final class McpDirectMessageService {

  private final IdentityVault identityVault;
  private final RelayPool relayPool;
  private final Set<String> aliasesPermittedToDecrypt;

  /**
   * @param identityVault holds the keys and builds the signing collaborator
   * @param relayPool the connections used to deliver
   * @param aliasesPermittedToDecrypt identities whose owner has allowed reading their messages
   */
  public McpDirectMessageService(
      @NonNull IdentityVault identityVault,
      @NonNull RelayPool relayPool,
      @NonNull Set<String> aliasesPermittedToDecrypt) {
    this.identityVault = identityVault;
    this.relayPool = relayPool;
    this.aliasesPermittedToDecrypt = Set.copyOf(aliasesPermittedToDecrypt);
  }

  /**
   * Send a message to each recipient's own relays.
   *
   * @param alias the identity to send as
   * @param recipients who to send to
   * @param content the message text
   * @return one outcome per participant, the sender included
   */
  public List<RecipientDeliveryOutcome> send(
      @NonNull String alias, @NonNull List<PublicKey> recipients, @NonNull String content) {
    ChatMessage message =
        ChatMessage.builder()
            .from(identityVault.publicKeyOf(alias))
            .to(recipients)
            .content(content)
            .build();
    return publisherFor(alias).send(message);
  }

  /**
   * Unwrap a gift wrap addressed to an identity.
   *
   * @param alias the identity the wrap is addressed to
   * @param giftWrap the received wrap
   * @return the message inside
   * @throws nostr.mcp.tool.ToolException when this identity may not decrypt
   */
  public ChatMessage read(@NonNull String alias, @NonNull GenericEvent giftWrap) {
    refuseIfDecryptionNotPermitted(alias);
    return publisherFor(alias).read(giftWrap);
  }

  /**
   * Whether this identity's owner has allowed the model to read their messages.
   *
   * @param alias the identity to check
   * @return true when decryption is permitted
   */
  public boolean mayDecrypt(@NonNull String alias) {
    return aliasesPermittedToDecrypt.contains(alias);
  }

  /**
   * The public key an identity receives messages at.
   *
   * @param alias the identity
   * @return its public key
   */
  public PublicKey publicKeyOf(@NonNull String alias) {
    return identityVault.publicKeyOf(alias);
  }

  private void refuseIfDecryptionNotPermitted(String alias) {
    if (!mayDecrypt(alias)) {
      throw ToolFailure.WRITE_FORBIDDEN.raise(
          "Reading '"
              + alias
              + "' private messages is not enabled. Decrypting them would put private"
              + " correspondence into this conversation, so it must be allowed explicitly with"
              + " nostr.mcp.dm.decrypt-for=" + alias + ".");
    }
  }

  /**
   * Builds the publisher inside the vault, so the identity never crosses this boundary.
   */
  private DirectMessagePublisher publisherFor(String alias) {
    return identityVault.using(
        alias,
        identity ->
            new DirectMessagePublisher(
                new Nip17DirectMessageService(identity), new RelayListLookup(relayPool), relayPool));
  }
}
