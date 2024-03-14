package nostr.id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.util.NostrException;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class CustomIdentity extends AbstractBaseIdentity {

    @ToString.Exclude
    @NonNull
    private PrivateKey privateKey;
    private final String name;

    public CustomIdentity(@NonNull String name) {
        this.name = name;
        this.init();
    }

    private void init() {
        try {
            var config = new CustomIdentityConfiguration(name);
            this.privateKey = config.getPrivateKey();
        } catch (NostrException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    static class CustomIdentityConfiguration extends Identity.IdentityConfiguration {

        CustomIdentityConfiguration(@NonNull String name) throws IOException {
            super(name);
        }
    }
}
