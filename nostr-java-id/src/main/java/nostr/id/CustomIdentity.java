package nostr.id;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.util.NostrException;

import java.io.IOException;

@Data
@ToString
public class CustomIdentity implements IIdentity {

    @ToString.Exclude
    @NonNull
    private PrivateKey privateKey;
    private final String name;
    private PublicKey publicKey;

    public CustomIdentity(@NonNull String name) {
        this.name = name;
        this.init();
    }

    private void init() {
        try {
            var config = new CustomIdentityConfiguration(name);
            this.privateKey = config.getPrivateKey();
            this.publicKey = new IdentityHelper(this).getPublicKey();
        } catch (NostrException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    // TODO
    @Override
    public Signature sign(@NonNull ISignable signable) throws NostrException {
        try {
            return new IdentityHelper(this).sign(signable);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
    }

    static class CustomIdentityConfiguration extends Identity.IdentityConfiguration {

        CustomIdentityConfiguration(@NonNull String name) throws IOException {
            super();
            var configFile = appConfig.getIdentityFolderProperties() + "/" + name + ".properties";
            configFile = configFile.startsWith("/") ? configFile : "/" + configFile;
            load(configFile);
        }

    }
}
