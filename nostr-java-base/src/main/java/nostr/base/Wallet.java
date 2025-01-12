package nostr.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class Wallet {

    @EqualsAndHashCode.Include
    private String id;

    private String name;
    private String description;
    private Integer balance;

    @EqualsAndHashCode.Include
    private String privateKey;

    @EqualsAndHashCode.Include
    private String unit;
    private Set<Mint> mints;
    private Set<Relay> relays;
    private Set<Token> tokens;

    public Wallet() {
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

    public void addToken(@NonNull Token token) {
        this.tokens.add(token);
        this.refreshBalance();
    }

    public void removeToken(@NonNull Token token) {
        this.tokens.remove(token);
        this.refreshBalance();
    }

    public void refreshBalance() {
        int total = 0;
        for (Token token : this.tokens) {
            total += token.calculateAmount();
        }
        this.setBalance(total);
    }
}
