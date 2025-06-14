package nostr.event.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.tag.EventTag;

import java.util.List;

@Data
@NoArgsConstructor
public class NutZap {

    private CashuMint mint;
    private List<CashuProof> proofs;
    private PublicKey recipient;
    private EventTag nutZappedEvent;

    public void addProof(@NonNull CashuProof cashuProof) {
        if (proofs == null) {
            proofs = new java.util.ArrayList<>();
        }
        proofs.add(cashuProof);
    }
}
