package nostr.id;

import nostr.base.PrivateKey;
import nostr.base.PublicKey;

public interface IIdentity {

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();
}
