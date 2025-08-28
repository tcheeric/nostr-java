package nostr.event.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.json.serializer.CashuTokenSerializer;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonSerialize(using = CashuTokenSerializer.class)
public class CashuToken {

  @EqualsAndHashCode.Include private CashuMint mint;

  @EqualsAndHashCode.Include private List<CashuProof> proofs;

  private List<String> destroyed;

  public CashuToken() {
    this.destroyed = new ArrayList<>();
  }

  public CashuToken(@NonNull CashuMint mint, @NonNull List<CashuProof> proofs) {
    this(mint, proofs, new ArrayList<>());
  }

  public void addDestroyed(@NonNull String eventId) {
    this.destroyed.add(eventId);
  }

  public void removeDestroyed(@NonNull String eventId) {
    this.destroyed.remove(eventId);
  }

  public Integer calculateAmount() {
    return proofs.stream().mapToInt(CashuProof::getAmount).sum();
  }
}
