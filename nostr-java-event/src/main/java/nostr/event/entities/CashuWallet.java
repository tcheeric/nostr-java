package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.Relay;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class CashuWallet {

  @EqualsAndHashCode.Include private String id;

  private String name;
  private String description;
  private Integer balance;

  @EqualsAndHashCode.Include private String privateKey;

  
  private final Set<CashuMint> mints;
  private final Map<String, Set<Relay>> relays;
  private Set<CashuToken> tokens;

  public CashuWallet() {
    this.balance = 0;
    this.mints = new HashSet<>();
    this.relays = new HashMap<>();
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

  public void addMint(@NonNull CashuMint mint) {
    this.mints.add(mint);
  }

  public void removeMint(@NonNull CashuMint mint) {
    this.mints.remove(mint);
  }

  public CashuMint getMint(@NonNull String mintUrl) {
    return this.mints.stream()
        .filter(mint -> mint.getUrl().equals(mintUrl))
        .findFirst()
        .orElse(null);
  }

  public void addRelay(@NonNull String unit, @NonNull Relay relay) {
    Set<Relay> relaySet = this.relays.get(unit);
    if (relaySet == null) {
      relaySet = new HashSet<>();
    }
    relaySet.add(relay);
    this.relays.put(unit, relaySet);
  }

  public void removeRelay(@NonNull String unit, @NonNull Relay relay) {
    Set<Relay> relaySet = this.relays.get(unit);
    if (relaySet == null) {
      return;
    }

    relaySet.remove(relay);
    if (relaySet.isEmpty()) {
      this.relays.remove(unit);
    } else {
      this.relays.put(unit, relaySet);
    }
  }

  public Set<Relay> getRelays(@NonNull String unit) {
    return this.relays.get(unit);
  }

  public void refreshBalance() {
    int total = 0;
    for (CashuToken token : this.tokens) {
      total += token.calculateAmount();
    }
    this.setBalance(total);
  }
}
