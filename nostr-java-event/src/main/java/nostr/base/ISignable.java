package nostr.base;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author squirrel
 */
public interface ISignable {
  Signature getSignature();

  void setSignature(Signature signature);

  Consumer<Signature> getSignatureConsumer();

  Supplier<ByteBuffer> getByteArraySupplier();
}
