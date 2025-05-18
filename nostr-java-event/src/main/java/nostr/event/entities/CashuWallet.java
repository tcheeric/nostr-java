package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.Relay;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class CashuWallet {

    @EqualsAndHashCode.Include
    private String id;

    private String name;
    private String description;
    private Integer balance;

    @EqualsAndHashCode.Include
    private String privateKey;

    @EqualsAndHashCode.Include
    private String unit;
    private Set<CashuMint> mints;
    private Set<Relay> relays;
    private Set<CashuToken> tokens;

    public CashuWallet() {
        this.balance = 0;
        this.mints = new HashSet<>();
        this.relays = new HashSet<>();
        this.tokens = new HashSet<>();
    }

    public void reset() {
        this.resetBalance();
        this.tokens = new HashSet<>();
    }

    public void resetBalance() {
        this.balance = 0;
    }

    public void increaseBalance(Integer amount) {
        this.balance += amount;
    }

    public void decreaseBalance(Integer amount) {
        this.balance -= amount;
    }

    public void addToken(@NonNull CashuToken token) {
        this.tokens.add(token);
        this.refreshBalance();
    }

    public void removeToken(@NonNull CashuToken token) {
        this.tokens.remove(token);
        this.refreshBalance();
    }

    public void refreshBalance() {
        int total = 0;
        for (CashuToken token : this.tokens) {
            total += token.calculateAmount();
        }
        this.setBalance(total);
    }
}
