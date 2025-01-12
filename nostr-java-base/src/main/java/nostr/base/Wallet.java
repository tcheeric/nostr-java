package nostr.base;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    private String id;
    private String name;
    private String description;
    private Integer balance;
    private String privateKey;
    private String unit;
    private List<Mint> mints;
    private List<Relay> relays;
    private List<Token> tokens;

    public void resetBalance() {
        this.balance = 0;
    }

    public void increaseBalance(Integer amount) {
        this.balance += amount;
    }

    public void decreaseBalance(Integer amount) {
        this.balance -= amount;
    }
}
