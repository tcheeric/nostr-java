package nostr.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {
    private String id;
    private Long expiration;
    private Mint mint;
    private Wallet wallet;
}
