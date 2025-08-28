package nostr.event.entities;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.Relay;

@Data
@NoArgsConstructor
public class NutZapInformation {

  public List<Relay> relays = new ArrayList<>();
  public List<CashuMint> mints = new ArrayList<>();
  public String p2pkPubkey;
}
