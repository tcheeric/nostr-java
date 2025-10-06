package nostr.event.support;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import nostr.event.impl.GenericEvent;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes derived fields (serialized payload, id, timestamp) for {@link GenericEvent}.
 */
public final class GenericEventUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericEventUpdater.class);

  private GenericEventUpdater() {}

  public static void refresh(GenericEvent event) {
    try {
      event.setCreatedAt(Instant.now().getEpochSecond());
      byte[] serialized = GenericEventSerializer.serialize(event).getBytes(StandardCharsets.UTF_8);
      event.setSerializedEventCache(serialized);
      event.setId(NostrUtil.bytesToHex(NostrUtil.sha256(serialized)));
    } catch (NostrException | NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    } catch (AssertionError ex) {
      LOGGER.warn("Failed to update event during serialization: {}", ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }
}
