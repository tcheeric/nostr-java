package nostr.event.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.json.serializer.CashuTokenSerializer;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonSerialize(using = CashuTokenSerializer.class)
public class CashuToken {

  @EqualsAndHashCode.Include private CashuMint mint;

  @EqualsAndHashCode.Include
  @Builder.Default
  private List<CashuProof> proofs = new ArrayList<>();

  @Builder.Default private List<String> destroyed = new ArrayList<>();

  public CashuToken() {
    this.proofs = new ArrayList<>();
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
    if (proofs == null || proofs.isEmpty()) return 0;
    return proofs.stream().mapToInt(CashuProof::getAmount).sum();
  }

  /**
   * Number of destroyed event references recorded in this token.
   */
  public int getDestroyedCount() {
    return this.destroyed != null ? this.destroyed.size() : 0;
  }

  /**
   * Checks whether a destroyed event id is recorded.
   */
  public boolean containsDestroyed(@NonNull String eventId) {
    return this.destroyed != null && this.destroyed.contains(eventId);
  }
}
