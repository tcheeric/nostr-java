/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * Base event factory collecting sender, tags, and content to build events.
 */
@Data
public abstract class EventFactory<E extends GenericEvent, T extends BaseTag> {

  private final Identity identity;
  private final String content;
  private final List<T> tags;

  /**
   * Initialize the factory with a sender identity.
   */
  public EventFactory(Identity identity) {
    this(identity, new ArrayList<>(), "");
  }

  /** Default constructor with no sender, no tags, and empty content. */
  protected EventFactory() {
    this.identity = null;
    this.content = "";
    this.tags = new ArrayList<>();
  }

  /**
   * Initialize the factory with a sender and content.
   */
  public EventFactory(Identity sender, String content) {
    this(sender, new ArrayList<>(), content);
  }

  /**
   * Initialize the factory with a sender, tags and content.
   */
  public EventFactory(Identity sender, List<T> tags, String content) {
    this.content = content;
    this.tags = tags;
    this.identity = sender;
  }

  /** Build the event instance. */
  public abstract E create();

  /** Add a tag to the internal list. */
  protected void addTag(T tag) {
    this.tags.add(tag);
  }

  /** Return the sender public key if a sender is configured. */
  protected PublicKey getSender() {
    if (this.identity != null) {
      return this.identity.getPublicKey();
    }
    return null;
  }
}
