package nostr.crypto.schnorr;

import nostr.util.NostrUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  // Official BIP-340 test vectors (bip-0340/test-vectors.csv), indices 0 and 1.
  @Test
  void matchesBip340SigningVectors() throws Exception {
    assertBip340Vector(
        "0000000000000000000000000000000000000000000000000000000000000003",
        "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9",
        "0000000000000000000000000000000000000000000000000000000000000000",
        "0000000000000000000000000000000000000000000000000000000000000000",
        "E907831F80848D1069A5371B402410364BDF1C5F8307B0084C55F1CE2DCA8215"
            + "25F66A4A85EA8B71E482A74F382D2CE5EBEEE8FDB2172F477DF4900D310536C0");
    assertBip340Vector(
        "B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF",
        "DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",
        "0000000000000000000000000000000000000000000000000000000000000001",
        "243F6A8885A308D313198A2E03707344A4093822299F31D0082EFA98EC4E6C89",
        "6896BD60EEAE296DB48A229FF71DFE071BDE413E6D43F917DC8DCF8C78DE3341"
            + "8906D11AC976ABCCB20B091292BFF4EA897EFCB639EA871CFA95F6DE339E4B0A");
  }

  // BIP-340 vector 5: the public key is not a point on the curve, so verification must fail.
  @Test
  void verifyRejectsPublicKeyNotOnCurve() throws Exception {
    byte[] pub =
        hex("EEFDEA4CDB677750A420FEE807EACF21EB9898AE79B9768766E4FAA04A2D4A34");
    byte[] msg =
        hex("243F6A8885A308D313198A2E03707344A4093822299F31D0082EFA98EC4E6C89");
    byte[] sig =
        hex(
            "6CFF5C3BA86C69EA4B7376F31A9BCB4F74C1976089B2D9963DA2E5543E177769"
                + "69E89B4C5564D00349106B8497785DD7D1D713A8AE82B32FA79D5F7FC407D39B");
    assertFalse(Schnorr.verify(msg, pub, sig));
  }

  private static void assertBip340Vector(
      String secKey, String pubKey, String aux, String msg, String sig) throws Exception {
    byte[] sk = hex(secKey);
    byte[] m = hex(msg);
    assertEquals(pubKey.toLowerCase(), NostrUtil.bytesToHex(Schnorr.genPubKey(sk)));
    byte[] signature = Schnorr.sign(m, sk, hex(aux));
    assertEquals(sig.toLowerCase(), NostrUtil.bytesToHex(signature));
    assertTrue(Schnorr.verify(m, hex(pubKey), signature));
  }

  // BIP-340 vectors use upper-case hex, which NostrUtil's validator rejects.
  private static byte[] hex(String s) {
    return java.util.HexFormat.of().parseHex(s);
  }
}
