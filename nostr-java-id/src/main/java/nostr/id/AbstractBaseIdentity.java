package nostr.id;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.DelegationTag;
import nostr.util.NostrUtil;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Log
public abstract class AbstractBaseIdentity implements IIdentity {

    @Override
    public PublicKey getPublicKey() {
        try {
            return new PublicKey(Schnorr.genPubKey(this.getPrivateKey().getRawData()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Signature sign(@NonNull ISignable signable)  {
        if (signable instanceof GenericEvent genericEvent) {
            try {
                return signEvent(genericEvent);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (signable instanceof DelegationTag delegationTag) {
            try {
                return signDelegationTag(delegationTag);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        throw new RuntimeException();
    }

    private Signature signEvent(@NonNull GenericEvent event) throws Exception {
        event.update();
        log.log(Level.FINER, "Serialized event: {0}", new String(event.get_serializedEvent()));
        final var signedHashedSerializedEvent = Schnorr.sign(NostrUtil.sha256(event.get_serializedEvent()), this.getPrivateKey().getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedSerializedEvent);
        signature.setPubKey(getPublicKey());
        event.setSignature(signature);
        return signature;
    }

    private Signature signDelegationTag(@NonNull DelegationTag delegationTag) throws Exception {
        final var signedHashedToken = Schnorr.sign(NostrUtil.sha256(delegationTag.getToken().getBytes(StandardCharsets.UTF_8)), this.getPrivateKey().getRawData(), generateAuxRand());
        final Signature signature = new Signature();
        signature.setRawData(signedHashedToken);
        signature.setPubKey(getPublicKey());
        delegationTag.setSignature(signature);
        return signature;
    }

    private byte[] generateAuxRand() {
        return NostrUtil.createRandomByteArray(32);
    }

}
