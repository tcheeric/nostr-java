package nostr.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.Amount;
import nostr.event.entities.CashuMint;
import nostr.event.entities.CashuQuote;
import nostr.event.entities.CashuToken;
import nostr.event.entities.CashuWallet;
import nostr.event.entities.SpendingHistory;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.json.codec.EventEncodingException;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nostr.base.json.EventJsonMapper.mapper;

/**
 * NIP-60: Cashu Wallet over Nostr.
 *
 * <p>This class provides utilities for managing Cashu wallets on Nostr. Cashu is an ecash system
 * for Bitcoin that enables private, custodial Bitcoin wallets. NIP-60 defines how to store and
 * manage Cashu tokens, wallet metadata, transaction history, and quotes on Nostr relays.
 *
 * <h2>What is Cashu?</h2>
 *
 * <p>Cashu is a Chaumian ecash system for Bitcoin:
 * <ul>
 *   <li><strong>Ecash tokens:</strong> Bearer instruments backed by Bitcoin (like digital cash)</li>
 *   <li><strong>Blind signatures:</strong> Mint can't link tokens to users (privacy)</li>
 *   <li><strong>Custodial:</strong> Tokens are backed by Bitcoin held by the mint</li>
 *   <li><strong>Transferable:</strong> Tokens can be sent peer-to-peer offline</li>
 *   <li><strong>Lightweight:</strong> No blockchain, instant transactions</li>
 * </ul>
 *
 * <h2>What is NIP-60?</h2>
 *
 * <p>NIP-60 defines how to store Cashu wallet data on Nostr:
 * <ul>
 *   <li><strong>Wallet events (kind 37375):</strong> Wallet configuration and mint URLs</li>
 *   <li><strong>Token events (kind 7375):</strong> Unspent Cashu tokens (proofs)</li>
 *   <li><strong>History events (kind 7376):</strong> Transaction history</li>
 *   <li><strong>Quote events (kind 7377):</strong> Reserved tokens for redemption</li>
 * </ul>
 *
 * <p>Benefits of storing Cashu on Nostr:
 * <ul>
 *   <li><strong>Backup:</strong> Tokens are backed up to relays (recover lost wallet)</li>
 *   <li><strong>Sync:</strong> Multiple devices can access the same wallet</li>
 *   <li><strong>Privacy:</strong> Events can be encrypted with NIP-04 or NIP-44</li>
 *   <li><strong>Portable:</strong> Move wallets between clients</li>
 * </ul>
 *
 * <h2>Event Kinds</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Kind</th>
 *     <th>Name</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td><strong>37375</strong></td>
 *     <td>Wallet Event</td>
 *     <td>Wallet metadata: name, mints, relays, supported units</td>
 *   </tr>
 *   <tr>
 *     <td><strong>7375</strong></td>
 *     <td>Token Event</td>
 *     <td>Unspent Cashu tokens (proofs) linked to a wallet</td>
 *   </tr>
 *   <tr>
 *     <td><strong>7376</strong></td>
 *     <td>History Event</td>
 *     <td>Transaction history: send, receive, swap</td>
 *   </tr>
 *   <tr>
 *     <td><strong>7377</strong></td>
 *     <td>Quote Event</td>
 *     <td>Reserved tokens for Lightning redemption</td>
 *   </tr>
 * </table>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Create a Wallet Event</h3>
 * <pre>{@code
 * Identity walletOwner = new Identity("nsec1...");
 *
 * CashuWallet wallet = CashuWallet.builder()
 *     .name("My Cashu Wallet")
 *     .mints(List.of(
 *         new CashuMint("https://mint.minibits.cash/Bitcoin", List.of("sat", "msat"))
 *     ))
 *     .relays(List.of(new Relay("wss://relay.damus.io")))
 *     .unit("sat")
 *     .build();
 *
 * NIP60 nip60 = new NIP60(walletOwner);
 * nip60.createWalletEvent(wallet)
 *      .sign()
 *      .send(relays);
 * }</pre>
 *
 * <h3>Example 2: Create a Token Event (Store Unspent Tokens)</h3>
 * <pre>{@code
 * Identity walletOwner = new Identity("nsec1...");
 * CashuWallet wallet = ... // existing wallet
 *
 * CashuToken token = CashuToken.builder()
 *     .mint("https://mint.minibits.cash/Bitcoin")
 *     .proofs(List.of(...)) // list of proofs from the mint
 *     .build();
 *
 * NIP60 nip60 = new NIP60(walletOwner);
 * nip60.createTokenEvent(token, wallet)
 *      .sign()
 *      .send(relays); // backup tokens to relays
 * }</pre>
 *
 * <h3>Example 3: Create a Spending History Event</h3>
 * <pre>{@code
 * Identity walletOwner = new Identity("nsec1...");
 * CashuWallet wallet = ... // existing wallet
 *
 * SpendingHistory history = SpendingHistory.builder()
 *     .direction("out") // "in" or "out"
 *     .amount(new Amount(1000, "sat"))
 *     .timestamp(System.currentTimeMillis() / 1000)
 *     .description("Paid for coffee")
 *     .build();
 *
 * NIP60 nip60 = new NIP60(walletOwner);
 * nip60.createSpendingHistoryEvent(history, wallet)
 *      .sign()
 *      .send(relays);
 * }</pre>
 *
 * <h3>Example 4: Create a Redemption Quote Event</h3>
 * <pre>{@code
 * Identity walletOwner = new Identity("nsec1...");
 *
 * CashuQuote quote = CashuQuote.builder()
 *     .quoteId("quote_abc123")
 *     .amount(new Amount(5000, "sat"))
 *     .mint("https://mint.minibits.cash/Bitcoin")
 *     .request("lnbc5000n...") // Lightning invoice
 *     .state("pending") // pending, paid, unpaid
 *     .build();
 *
 * NIP60 nip60 = new NIP60(walletOwner);
 * nip60.createRedemptionQuoteEvent(quote)
 *      .sign()
 *      .send(relays);
 * }</pre>
 *
 * <h2>Key Concepts</h2>
 *
 * <h3>Cashu Proofs</h3>
 * <p>Cashu proofs are the actual tokens. They are JSON objects containing:
 * <ul>
 *   <li><strong>id:</strong> Keyset ID (identifies the mint's keys)</li>
 *   <li><strong>amount:</strong> Token denomination (e.g., 1, 2, 4, 8, 16... sats)</li>
 *   <li><strong>secret:</strong> Random secret (proves ownership)</li>
 *   <li><strong>C:</strong> Blinded signature from the mint</li>
 * </ul>
 *
 * <h3>Mints</h3>
 * <p>Mints are custodians that issue Cashu tokens. Each mint:
 * <ul>
 *   <li>Holds Bitcoin reserves backing the tokens</li>
 *   <li>Signs tokens with blind signatures</li>
 *   <li>Redeems tokens for Bitcoin (Lightning)</li>
 *   <li>Can support multiple units (sat, msat, USD, EUR, etc.)</li>
 * </ul>
 *
 * <h3>Wallet Tags</h3>
 * <p>Wallet events use a 'd' tag to identify the wallet (like an address). Token, history, and
 * quote events reference this 'd' tag to associate data with a specific wallet.
 *
 * <h2>Security Considerations</h2>
 *
 * <ul>
 *   <li><strong>Encrypt events:</strong> Use NIP-04 or NIP-44 to encrypt token events (proofs are bearer instruments!)</li>
 *   <li><strong>Relay trust:</strong> Relays can see encrypted data but not decrypt it</li>
 *   <li><strong>Mint trust:</strong> Mints are custodial - they hold your Bitcoin</li>
 *   <li><strong>Backup regularly:</strong> Sync tokens to relays to prevent loss</li>
 *   <li><strong>Spent tokens:</strong> Delete spent token events to avoid confusion</li>
 * </ul>
 *
 * <h2>Design Pattern</h2>
 *
 * <p>This class follows the <strong>Facade Pattern</strong>:
 * <ul>
 *   <li>Simplifies creation of NIP-60 events (wallet, token, history, quote)</li>
 *   <li>Delegates to {@link GenericEventFactory} for event construction</li>
 *   <li>Uses entity classes ({@link CashuWallet}, {@link CashuToken}, {@link SpendingHistory}, {@link CashuQuote})</li>
 *   <li>Provides static helper methods for tag creation</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This class is <strong>not thread-safe</strong> for instance methods. Each thread should create
 * its own {@code NIP60} instance. Static methods are thread-safe.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/60.md">NIP-60 Specification</a>
 * @see <a href="https://docs.cashu.space">Cashu Documentation</a>
 * @see CashuWallet
 * @see CashuToken
 * @see SpendingHistory
 * @see CashuQuote
 * @since 0.6.0
 */
public class NIP60 extends EventNostr {

  public NIP60(@NonNull Identity sender) {
    setSender(sender);
  }

  public NIP60 createWalletEvent(@NonNull CashuWallet wallet) {
    GenericEvent walletEvent =
        new GenericEventFactory(
                getSender(),
                Kind.WALLET.getValue(),
                getWalletEventTags(wallet),
                getWalletEventContent(wallet))
            .create();
    updateEvent(walletEvent);
    return this;
  }

  public NIP60 createTokenEvent(@NonNull CashuToken token, @NonNull CashuWallet wallet) {
    GenericEvent tokenEvent =
        new GenericEventFactory(
                getSender(),
                Kind.WALLET_UNSPENT_PROOF.getValue(),
                getTokenEventTags(wallet),
                getTokenEventContent(token))
            .create();
    updateEvent(tokenEvent);
    return this;
  }

  public NIP60 createSpendingHistoryEvent(
      @NonNull SpendingHistory spendingHistory, @NonNull CashuWallet wallet) {
    GenericEvent spendingHistoryEvent =
        new GenericEventFactory(
                getSender(),
                Kind.WALLET_TX_HISTORY.getValue(),
                getSpendingHistoryEventTags(wallet),
                getSpendingHistoryEventContent(spendingHistory))
            .create();
    updateEvent(spendingHistoryEvent);
    return this;
  }

  public NIP60 createRedemptionQuoteEvent(@NonNull CashuQuote quote) {
    GenericEvent redemptionQuoteEvent =
        new GenericEventFactory(
                getSender(),
                Kind.RESERVED_CASHU_WALLET_TOKENS.getValue(),
                getRedemptionQuoteEventTags(quote),
                getRedemptionQuoteEventContent(quote))
            .create();
    updateEvent(redemptionQuoteEvent);
    return this;
  }

  /**
   * Create a mint tag for a Cashu mint reference.
   *
   * @param mint the Cashu mint (contains URL and supported units)
   * @return the created mint tag
   */
  public static BaseTag createMintTag(@NonNull CashuMint mint) {
    return createMintTag(
        mint.getUrl(), mint.getUnits() != null ? mint.getUnits().toArray(new String[0]) : null);
  }

  /**
   * Create a mint tag for a Cashu mint reference.
   *
   * @param mintUrl the mint base URL
   * @return the created mint tag
   */
  public static BaseTag createMintTag(@NonNull String mintUrl) {
    return createMintTag(mintUrl, (String[]) null);
  }

  /**
   * Create a mint tag for a Cashu mint reference with supported units.
   *
   * @param mintUrl the mint base URL
   * @param units optional list of supported unit codes
   * @return the created mint tag
   */
  public static BaseTag createMintTag(@NonNull String mintUrl, String... units) {
    List<String> params = new ArrayList<>();
    params.add(mintUrl);
    if (units != null && units.length > 0) {
      params.addAll(Arrays.asList(units));
    }
    return new BaseTagFactory(Constants.Tag.MINT_CODE, params.toArray(new String[0])).create();
  }

  /**
   * Create a unit tag for Cashu amounts.
   *
   * @param unit the currency/unit code (e.g., sat, usd)
   * @return the created unit tag
   */
  public static BaseTag createUnitTag(@NonNull String unit) {
    return new BaseTagFactory(Constants.Tag.UNIT_CODE, unit).create();
  }

  /**
   * Create a wallet private key tag.
   *
   * @param privKey the wallet private key
   * @return the created tag
   */
  public static BaseTag createPrivKeyTag(@NonNull String privKey) {
    return new BaseTagFactory(Constants.Tag.PRIVKEY_CODE, privKey).create();
  }

  /**
   * Create a balance tag for a given unit.
   *
   * @param balance the wallet balance value
   * @param unit the currency/unit code
   * @return the created balance tag
   */
  public static BaseTag createBalanceTag(@NonNull Integer balance, String unit) {
    return new BaseTagFactory(Constants.Tag.BALANCE_CODE, balance.toString(), unit).create();
  }

  /**
   * Create a direction tag for spending history entries.
   *
   * @param direction the spending direction (incoming/outgoing)
   * @return the created direction tag
   */
  public static BaseTag createDirectionTag(@NonNull SpendingHistory.Direction direction) {
    return new BaseTagFactory(Constants.Tag.DIRECTION_CODE, direction.getValue()).create();
  }

  public static BaseTag createAmountTag(@NonNull Amount amount) {
    return new BaseTagFactory(
            Constants.Tag.AMOUNT_CODE, amount.getAmount().toString(), amount.getUnit())
        .create();
  }

  public static BaseTag createExpirationTag(@NonNull Long expiration) {
    return new BaseTagFactory(Constants.Tag.EXPIRATION_CODE, expiration.toString()).create();
  }

  private String getWalletEventContent(@NonNull CashuWallet wallet) {
    List<BaseTag> tags = new ArrayList<>();
    Map<String, Set<Relay>> relayMap = wallet.getRelays();
    Set<String> unitSet = relayMap.keySet();
    unitSet.forEach(u -> tags.add(NIP60.createBalanceTag(wallet.getBalance(), u)));
    tags.add(NIP60.createPrivKeyTag(wallet.getPrivateKey()));

    try {
      return NIP44.encrypt(
          getSender(), mapper().writeValueAsString(tags), getSender().getPublicKey());
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode wallet content", ex);
    }
  }

  private String getTokenEventContent(@NonNull CashuToken token) {
    try {
      return NIP44.encrypt(
          getSender(), mapper().writeValueAsString(token), getSender().getPublicKey());
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode token content", ex);
    }
  }

  private String getRedemptionQuoteEventContent(@NonNull CashuQuote quote) {
    return NIP44.encrypt(getSender(), quote.getId(), getSender().getPublicKey());
  }

  private String getSpendingHistoryEventContent(@NonNull SpendingHistory spendingHistory) {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(NIP60.createDirectionTag(spendingHistory.getDirection()));
    tags.add(NIP60.createAmountTag(spendingHistory.getAmount()));
    tags.addAll(spendingHistory.getEventTags());

    return NIP44.encrypt(getSender(), getContent(tags), getSender().getPublicKey());
  }

  /**
   * Encodes a list of tags to JSON array format.
   *
   * <p>Note: This could be extracted to a GenericTagListEncoder class if this pattern
   * is used in multiple places. For now, it's kept here as it's NIP-60 specific.
   */
  private String getContent(@NonNull List<BaseTag> tags) {
    return "["
        + tags.stream()
            .map(tag -> new BaseTagEncoder(tag).encode())
            .collect(Collectors.joining(","))
        + "]";
  }

  private List<BaseTag> getWalletEventTags(@NonNull CashuWallet wallet) {
    List<BaseTag> tags = new ArrayList<>();

    Map<String, Set<Relay>> relayMap = wallet.getRelays();
    Set<String> unitSet = relayMap.keySet();
    unitSet.forEach(
        u -> {
          tags.add(NIP60.createUnitTag(u));
          tags.add(NIP60.createBalanceTag(wallet.getBalance(), u));
        });

    tags.add(NIP01.createIdentifierTag(wallet.getId()));
    tags.add(NIP57.createDescriptionTag(wallet.getDescription()));
    tags.add(NIP60.createPrivKeyTag(wallet.getPrivateKey()));

    if (wallet.getMints() != null) {
      wallet.getMints().forEach(mint -> tags.add(NIP60.createMintTag(mint)));
    }

    Map<String, Set<Relay>> relays = wallet.getRelays();
    relays
        .keySet()
        .forEach(
            unit -> {
              Set<Relay> relaySet = wallet.getRelays(unit);
              relaySet.forEach(
                  relay -> {
                    tags.add(NIP42.createRelayTag(relay));
                  });
            });

    return tags;
  }

  private List<BaseTag> getTokenEventTags(@NonNull CashuWallet wallet) {
    List<BaseTag> tags = new ArrayList<>();

    tags.add(
        NIP01.createAddressTag(
            Kind.WALLET.getValue(),
            getSender().getPublicKey(),
            NIP01.createIdentifierTag(wallet.getId()),
            null));

    return tags;
  }

  private List<BaseTag> getSpendingHistoryEventTags(@NonNull CashuWallet wallet) {
    return getTokenEventTags(wallet);
  }

  private List<BaseTag> getRedemptionQuoteEventTags(@NonNull CashuQuote quote) {
    List<BaseTag> tags = new ArrayList<>();
    tags.add(NIP60.createExpirationTag(quote.getExpiration()));
    tags.add(NIP60.createMintTag(quote.getMint()));
    tags.add(
        NIP01.createAddressTag(
            Kind.WALLET.getValue(),
            getSender().getPublicKey(),
            NIP01.createIdentifierTag(quote.getWallet().getId()),
            null));
    return tags;
  }
}
