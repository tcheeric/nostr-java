package nostr.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.NIP60Impl;
import nostr.api.factory.impl.NIP60Impl.SpendingHistoryEventFactory;
import nostr.api.factory.impl.NIP60Impl.TokenEventFactory;
import nostr.api.factory.impl.NIP60Impl.WalletEventFactory;
import nostr.base.Mint;
import nostr.base.Quote;
import nostr.base.Token;
import nostr.base.Wallet;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

public class NIP60<T extends GenericEvent> extends EventNostr<T> {

    private static final String MINT_TAG_NAME = "mint";
    private static final String UNIT_TAG_NAME = "unit";
    private static final String PRIVKEY_TAG_NAME = "privkey";
    private static final String BALANCE_TAG_NAME = "balance";
    private static final String DIRECTION_TAG_NAME = "direction";
    private static final String AMOUNT_TAG_NAME = "amount";
    private static final String EXPIRATION_TAG_NAME = "expiration";

    public NIP60(@NonNull Identity sender) {
        setSender(sender);
    }

    @SuppressWarnings("unchecked")
    public NIP60<T> createWalletEvent(@NonNull Wallet wallet) {
        setEvent((T) new WalletEventFactory(getSender(), getWalletEventTags(wallet), getWalletEventContent(wallet))
                .create());
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60<T> createTokenEvent(@NonNull Token token, @NonNull Wallet wallet) {
        setEvent((T) new TokenEventFactory(getSender(), getTokenEventTags(wallet), getTokenEventContent(token))
                .create());
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60<T> createSpendingHistoryEvent(@NonNull SpendingHistory spendingHistory, @NonNull Wallet wallet) {
        setEvent((T) new SpendingHistoryEventFactory(getSender(), getSpendingHistoryEventTags(wallet),
                getSpendingHistoryEventContent(spendingHistory))
                .create());
        return this;
    }

    @SuppressWarnings("unchecked")
    public NIP60<T> createRedemptionQuoteEvent(@NonNull Quote quote) {
        setEvent((T) new NIP60Impl.RedemptionQuoteEventFactory(getSender(), getRedemptionQuoteEventTags(quote),
                getRedemptionQuoteEventContent(quote))
                .create());
        return this;
    }

    /**
     * @param mint
     * @return
     */
    public static GenericTag createMintTag(@NonNull Mint mint) {
        List<String> units = mint.getUnits();
        return createMintTag(mint.getUrl(), units != null ? units.toArray(new String[0]) : null);
    }

    /**
     * @param mintUrl
     * @return
     */
    public static GenericTag createMintTag(@NonNull String mintUrl) {
        return createMintTag(mintUrl, (String[]) null);
    }

    /**
     * @param mintUrl
     * @param units
     * @return
     */
    public static GenericTag createMintTag(@NonNull String mintUrl, String... units) {
        List<String> params = new ArrayList<>();
        params.add(mintUrl);
        if (units != null && units.length > 0) {
            params.addAll(Arrays.asList(units));
        }
        return new TagFactory(MINT_TAG_NAME, 60, params.toArray(new String[0])).create();
    }

    /**
     * @param unit
     * @return
     */
    public static GenericTag createUnitTag(@NonNull String unit) {
        return new TagFactory(UNIT_TAG_NAME, 60, unit).create();
    }

    /**
     * @param privKey
     * @return
     */
    public static GenericTag createPrivKeyTag(@NonNull String privKey) {
        return new TagFactory(PRIVKEY_TAG_NAME, 60, privKey).create();
    }

    /**
     * @param balance
     * @param unit
     * @return
     */
    public static GenericTag createBalanceTag(@NonNull Integer balance, String unit) {
        return new TagFactory(BALANCE_TAG_NAME, 60, balance.toString(), unit).create();
    }

    public static GenericTag createDirectionTag(@NonNull NIP60.SpendingHistory.Direction direction) {
        return new TagFactory(DIRECTION_TAG_NAME, 60, direction.getValue()).create();
    }

    public static GenericTag createAmountTag(@NonNull SpendingHistory.Amount amount) {
        return new TagFactory(AMOUNT_TAG_NAME, 60, amount.getAmount().toString(), amount.getUnit()).create();
    }

    public static GenericTag createExpirationTag(@NonNull Long expiration) {
        return new TagFactory(EXPIRATION_TAG_NAME, 60, expiration.toString()).create();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpendingHistory {
        private Direction direction;
        private Amount amount;

        @Builder.Default
        private List<EventTag> eventTags = new ArrayList<>();

        public enum Direction {
            RECEIVED("in"),
            SENT("out");

            private final String value;

            Direction(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }

        public void addEventTag(@NonNull EventTag eventTag) {
            this.eventTags.add(eventTag);
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Amount {
            private Integer amount;
            private String unit;
        }
    }

    @SneakyThrows
    private String getWalletEventContent(@NonNull Wallet wallet) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP60.createBalanceTag(wallet.getBalance(), wallet.getUnit()));
        tags.add(NIP60.createPrivKeyTag(wallet.getPrivateKey()));

        return NIP44.encrypt(getSender(), new ObjectMapper().writeValueAsString(tags), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getTokenEventContent(@NonNull Token token) {
        return NIP44.encrypt(getSender(), new ObjectMapper().writeValueAsString(token), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getRedemptionQuoteEventContent(@NonNull Quote quote) {
        return NIP44.encrypt(getSender(), quote.getId(), getSender().getPublicKey());
    }

    @SneakyThrows
    private String getSpendingHistoryEventContent(@NonNull SpendingHistory spendingHistory) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP60.createDirectionTag(spendingHistory.getDirection()));
        tags.add(NIP60.createAmountTag(spendingHistory.getAmount()));
        spendingHistory.getEventTags().forEach(eventTag -> tags.add(eventTag));

        String content = getContent(tags);

        return NIP44.encrypt(getSender(), content, getSender().getPublicKey());
    }

    // TODO: Consider writing a GenericTagListEncoder class for this
    private String getContent(@NonNull List<BaseTag> tags) {
        return "[" + tags.stream()
                .map(tag -> new BaseTagEncoder(tag).encode())
                .collect(Collectors.joining(",")) + "]";
    }

    private List<BaseTag> getWalletEventTags(@NonNull Wallet wallet) {
        List<BaseTag> tags = new ArrayList<>();

        tags.add(NIP60.createUnitTag(wallet.getUnit()));
        tags.add(NIP01.createIdentifierTag(wallet.getId()));
        tags.add(NIP57.createDescriptionTag(wallet.getDescription()));
        tags.add(NIP60.createPrivKeyTag(wallet.getPrivateKey()));
        tags.add(NIP60.createBalanceTag(wallet.getBalance(), wallet.getUnit()));

        if (wallet.getMint() != null) {
            wallet.getMint().forEach(mint -> tags.add(NIP60.createMintTag(mint)));
        }

        if (wallet.getRelays() != null) {
            wallet.getRelays().forEach(relay -> tags.add(NIP42.createRelayTag(relay)));
        }

        return tags;
    }

    private List<BaseTag> getTokenEventTags(@NonNull Wallet wallet) {
        List<BaseTag> tags = new ArrayList<>();

        tags.add(NIP01.createAddressTag(37375, getSender().getPublicKey(), NIP01.createIdentifierTag(wallet.getId()),
                null));

        return tags;
    }

    private List<BaseTag> getSpendingHistoryEventTags(@NonNull Wallet wallet) {
        return getTokenEventTags(wallet);
    }

    private List<BaseTag> getRedemptionQuoteEventTags(@NonNull Quote quote) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(NIP60.createExpirationTag(quote.getExpiration()));
        tags.add(NIP60.createMintTag(quote.getMint()));
        tags.add(NIP01.createAddressTag(37375, getSender().getPublicKey(),
                NIP01.createIdentifierTag(quote.getWallet().getId()),
                null));
        return tags;
    }

}
