package nostr.crypto.schnorr;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import nostr.util.NostrUtil;
import org.junit.jupiter.api.Test;

/** Tests for Schnorr signing and verification helpers. */
public class SchnorrTest {

  @Test
  void signVerifyRoundtrip() throws Exception {
    byte[] priv = Schnorr.generatePrivateKey();
    byte[] pub = Schnorr.genPubKey(priv);
    byte[] msg = NostrUtil.createRandomByteArray(32);
    byte[] aux = NostrUtil.createRandomByteArray(32);

    byte[] sig = Schnorr.sign(msg, priv, aux);
    assertNotNull(sig);
    assertEquals(64, sig.length);
    assertTrue(Schnorr.verify(msg, pub, sig));
  }

  @Test
  void verifyFailsForDifferentMessage() throws Exception {
    byte[] priv = Schnorr.generatePrivateKey();
    byte[] pub = Schnorr.genPubKey(priv);
    byte[] msg1 = NostrUtil.createRandomByteArray(32);
    byte[] msg2 = NostrUtil.createRandomByteArray(32);
    byte[] aux = NostrUtil.createRandomByteArray(32);
    byte[] sig = Schnorr.sign(msg1, priv, aux);
    assertFalse(Schnorr.verify(msg2, pub, sig));
  }

  @Test
  void genPubKeyRejectsOutOfRangeKey() {
    byte[] zeros = new byte[32];
    assertThrows(SchnorrException.class, () -> Schnorr.genPubKey(zeros));
  }

  @Test
  void verifyRejectsInvalidLengths() throws Exception {
    byte[] priv = Schnorr.generatePrivateKey();
    byte[] pub = Schnorr.genPubKey(priv);
    byte[] msg = NostrUtil.createRandomByteArray(32);
    byte[] sig = Schnorr.sign(msg, priv, NostrUtil.createRandomByteArray(32));

    assertThrows(SchnorrException.class, () -> Schnorr.verify(new byte[31], pub, sig));
    assertThrows(SchnorrException.class, () -> Schnorr.verify(msg, new byte[31], sig));
    assertThrows(SchnorrException.class, () -> Schnorr.verify(msg, pub, new byte[63]));
  }
}

