package nostr.event.entities;

import lombok.Getter;

/**
 * @author squirrel
 */
@Getter
public enum Reaction {
  LIKE("+"),
  DISLIKE("-");

  private final String emoji;

  Reaction(String emoji) {
    this.emoji = emoji;
  }
}
