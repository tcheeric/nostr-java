package nostr.base;

import lombok.NonNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FNostrList<T> extends HashSet<T> {
  protected FNostrList() {
    super();
  }

  protected FNostrList(@NonNull T item) {
    super();
    this.add(item);
  }

  protected FNostrList(@NonNull List<T> items) {
    super();
    this.addAll(items);
  }

  protected FNostrList(@NonNull Set<T> uniqueItems) {
    super();
    this.addAllExclusive(uniqueItems);
  }

  public boolean addExclusive(@NonNull T elt) throws IllegalArgumentException {
    return Optional.of(super.add(elt)).orElseThrow(IllegalArgumentException::new);
  }

  public boolean addAllExclusive(@NonNull Set<T> elt) throws IllegalArgumentException {
    elt.forEach(this::addExclusive);
    return true;
  }

  public List<T> getList() {
    return super.stream().toList();
//      return super.toList();
//  TODO: unit tst this.getList()
  }
}
