package nostr.id;

import lombok.NonNull;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.util.NostrException;

public interface IIdentity {

    PrivateKey getPrivateKey();

    PublicKey getPublicKey();

    Signature sign(@NonNull ISignable signable);
}
