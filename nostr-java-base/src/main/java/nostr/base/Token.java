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
public class Token {
    private Mint mint;
    private List<Proof> proofs;

    public Integer calculateAmount() {
        return proofs.stream().mapToInt(Proof::getAmount).sum();
    }
}
