package nostr.api;

import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
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
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

public class NIP60 extends EventNostr {

    public NIP60(@NonNull Identity sender) {
        setSender(sender);
    }

    @SuppressWarnings("unchecked")
    public NIP60 createWalletEvent(@NonNull CashuWallet wallet) {
        GenericEvent walletEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_WALLET_EVENT, getWalletEventTags(wallet), getWalletEventContent(wallet)).create();
        updateEvent(walletEvent);
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60 createTokenEvent(@NonNull CashuToken token, @NonNull CashuWallet wallet) {
        GenericEvent tokenEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_WALLET_TOKENS, getTokenEventTags(wallet), getTokenEventContent(token)).create();
        updateEvent(tokenEvent);
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60 createSpendingHistoryEvent(@NonNull SpendingHistory spendingHistory, @NonNull CashuWallet wallet) {
        GenericEvent spendingHistoryEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_WALLET_HISTORY, getSpendingHistoryEventTags(wallet), getSpendingHistoryEventContent(spendingHistory)).create();
        updateEvent(spendingHistoryEvent);
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60 createRedemptionQuoteEvent(@NonNull CashuQuote quote) {
        GenericEvent redemptionQuoteEvent = new GenericEventFactory(getSender(), Constants.Kind.CASHU_RESERVED_WALLET_TOKENS, getRedemptionQuoteEventTags(quote), getRedemptionQuoteEventContent(quote)).create();
        updateEvent(redemptionQuoteEvent);
        return this;
    }

    /**
     * @param mint
     * @return
     */
    public static BaseTag createMintTag(@NonNull CashuMint mint) {
        List<String> units = mint.getUnits();
        return createMintTag(mint.getUrl(), units != null ? units.toArray(new String[0]) : null);
    }

    /**
     * @param mintUrl
     * @return
     */
    public static BaseTag createMintTag(@NonNull String mintUrl) {
        return createMintTag(mintUrl, (String[]) null);
    }

    /**
     * @param mintUrl
     * @param units
     * @return
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
     * @param unit
     * @return
     */
    public static BaseTag createUnitTag(@NonNull String unit) {
        return new BaseTagFactory(Constants.Tag.UNIT_CODE, unit).create();
    }

    /**
     * @param privKey
     * @return
     */
    public static BaseTag createPrivKeyTag(@NonNull String privKey) {
        return new BaseTagFactory(Constants.Tag.PRIVKEY_CODE, privKey).create();
    }

    /**
     * @param balance
     * @param unit
     * @return
     */
    public static BaseTag createBalanceTag(@NonNull Integer balance, String unit) {
        return new BaseTagFactory(Constants.Tag.BALANCE_CODE, balance.toString(), unit).create();
    }

    public static BaseTag createDirectionTag(@NonNull SpendingHistory.Direction direction) {
        return new BaseTagFactory(Constants.Tag.DIRECTION_CODE, direction.getValue()).create();
    }

    public static BaseTag createAmountTag(@NonNull Amount amount) {
        return new BaseTagFactory(Constants.Tag.AMOUNT_CODE, amount.getAmount().toString(), amount.getUnit()).create();
    }

    public static BaseTag createExpirationTag(@NonNull Long expiration) {
        return new BaseTagFactory(Constants.Tag.EXPIRATION_CODE, expiration.toString()).create();
    }

    @SneakyThrows
    private String getWalletEventContent(@NonNull CashuWallet wallet) {
        List<BaseTag> tags = new ArrayList<>();
        Map<String, Set<Relay>> relayMap = wallet.getRelays();
        Set<String> unitSet = relayMap.keySet();
        unitSet.forEach(u -> tags.add(NIP60.createBalanceTag(wallet.getBalance(), u)));
        tags.add(NIP60.createPrivKeyTag(wallet.getPrivateKey()));

        return NIP44.encrypt(getSender(), MAPPER_AFTERBURNER.writeValueAsString(tags), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getTokenEventContent(@NonNull CashuToken token) {
        return NIP44.encrypt(getSender(), MAPPER_AFTERBURNER.writeValueAsString(token), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getRedemptionQuoteEventContent(@NonNull CashuQuote quote) {
        return NIP44.encrypt(getSender(), quote.getId(), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getSpendingHistoryEventContent(@NonNull SpendingHistory spendingHistory) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP60.createDirectionTag(spendingHistory.getDirection()));
        tags.add(NIP60.createAmountTag(spendingHistory.getAmount()));
        tags.addAll(spendingHistory.getEventTags());

        String content = getContent(tags);

        return NIP44.encrypt(getSender(), content, getSender().getPublicKey());
    }

    // TODO: Consider writing a GenericTagListEncoder class for this
    private String getContent(@NonNull List<BaseTag> tags) {
        return "[" + tags.stream()
                .map(tag -> new BaseTagEncoder(tag).encode())
                .collect(Collectors.joining(",")) + "]";
    }

    private List<BaseTag> getWalletEventTags(@NonNull CashuWallet wallet) {
        List<BaseTag> tags = new ArrayList<>();

        Map<String, Set<Relay>> relayMap = wallet.getRelays();
        Set<String> unitSet = relayMap.keySet();
        unitSet.forEach(u -> {
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
        relays.keySet().forEach(unit -> {
            Set<Relay> relaySet = wallet.getRelays(unit);
            relaySet.forEach(relay -> {
                tags.add(NIP42.createRelayTag(relay));
            });
        });

        return tags;
    }

    private List<BaseTag> getTokenEventTags(@NonNull CashuWallet wallet) {
        List<BaseTag> tags = new ArrayList<>();

        tags.add(NIP01.createAddressTag(Constants.Kind.CASHU_WALLET_EVENT, getSender().getPublicKey(), NIP01.createIdentifierTag(wallet.getId()), null));

        return tags;
    }

    private List<BaseTag> getSpendingHistoryEventTags(@NonNull CashuWallet wallet) {
        return getTokenEventTags(wallet);
    }

    private List<BaseTag> getRedemptionQuoteEventTags(@NonNull CashuQuote quote) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP60.createExpirationTag(quote.getExpiration()));
        tags.add(NIP60.createMintTag(quote.getMint()));
        tags.add(NIP01.createAddressTag(Constants.Kind.CASHU_WALLET_EVENT, getSender().getPublicKey(),
                NIP01.createIdentifierTag(quote.getWallet().getId()),
                null));
        return tags;
    }

}
