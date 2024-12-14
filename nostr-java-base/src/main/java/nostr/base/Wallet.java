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
public class Wallet {

    private String id;
    private String name;
    private String description;
    private Integer balance;
    private String privateKey;
    private String unit;
    private List<Mint> mint;
    private List<Relay> relays;
}
