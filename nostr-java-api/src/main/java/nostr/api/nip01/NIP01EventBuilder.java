package nostr.api.nip01;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.Kind;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.List;

/**
 * Builds common NIP-01 events while keeping {@link nostr.api.NIP01} focused on orchestration.
 */
public final class NIP01EventBuilder {

  private Identity defaultSender;

  public NIP01EventBuilder(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public void updateDefaultSender(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public GenericEvent buildTextNote(String content) {
    return buildTextNote(null, content);
  }

  public GenericEvent buildTextNote(Identity sender, String content) {
    return new GenericEventFactory(resolveSender(sender), Kind.TEXT_NOTE.getValue(), content)
        .create();
  }

  public GenericEvent buildRecipientTextNote(String content, List<PubKeyTag> tags) {
    return new GenericEventFactory<>(resolveSender(null), Kind.TEXT_NOTE.getValue(), tags, content)
        .create();
  }

  public GenericEvent buildTaggedTextNote(@NonNull List<BaseTag> tags, @NonNull String content) {
    return new GenericEventFactory<>(resolveSender(null), Kind.TEXT_NOTE.getValue(), tags, content)
        .create();
  }

  public GenericEvent buildMetadataEvent(@NonNull Identity sender, @NonNull String payload) {
    return new GenericEventFactory(resolveSender(sender), Kind.SET_METADATA.getValue(), payload)
        .create();
  }

  public GenericEvent buildMetadataEvent(@NonNull String payload) {
    Identity sender = resolveSender(null);
    if (sender != null) {
      return buildMetadataEvent(sender, payload);
    }
    return new GenericEventFactory(Kind.SET_METADATA.getValue(), payload).create();
  }

  public GenericEvent buildReplaceableEvent(Integer kind, String content) {
    return buildReplaceableEvent(null, kind, content);
  }

  public GenericEvent buildReplaceableEvent(
      Identity sender, Integer kind, String content) {
    return new GenericEventFactory(resolveSender(sender), kind, content).create();
  }

  public GenericEvent buildReplaceableEvent(
      List<BaseTag> tags, Integer kind, String content) {
    return buildReplaceableEvent(null, tags, kind, content);
  }

  public GenericEvent buildReplaceableEvent(List<BaseTag> tags, Integer kind, String content) {
    return new GenericEventFactory<>(resolveSender(null), kind, tags, content).create();
  }

  public GenericEvent buildEphemeralEvent(List<BaseTag> tags, Integer kind, String content) {
    return new GenericEventFactory<>(resolveSender(null), kind, tags, content).create();
  }

  public GenericEvent buildEphemeralEvent(Integer kind, String content) {
    return buildEphemeralEvent(null, kind, content);
  }

  public GenericEvent buildEphemeralEvent(Identity sender, Integer kind, String content) {
    return new GenericEventFactory(resolveSender(sender), kind, content).create();
  }

  public GenericEvent buildAddressableEvent(Integer kind, String content) {
    return buildAddressableEvent(null, kind, content);
  }

  public GenericEvent buildAddressableEvent(
      Identity sender, Integer kind, String content) {
    return new GenericEventFactory(resolveSender(sender), kind, content).create();
  }

  public GenericEvent buildAddressableEvent(
      @NonNull List<GenericTag> tags, @NonNull Integer kind, String content) {
    return new GenericEventFactory<>(resolveSender(null), kind, tags, content).create();
  }

  private Identity resolveSender(Identity override) {
    return override != null ? override : defaultSender;
  }
}
