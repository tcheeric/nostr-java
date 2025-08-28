package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashuQuote {
  private String id;
  private Long expiration;
  private CashuMint mint;
  private CashuWallet wallet;
}
