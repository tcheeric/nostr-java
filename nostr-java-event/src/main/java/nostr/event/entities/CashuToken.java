package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashuToken {
    private CashuMint mint;
    private List<CashuProof> proofs;

    public Integer calculateAmount() {
        return proofs.stream().mapToInt(CashuProof::getAmount).sum();
    }
}
