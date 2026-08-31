package nostr.mcp.identity;

import lombok.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Remembers which aliases exist in a store that cannot enumerate itself.
 *
 * <p>An OS keychain answers "give me the secret for this name" but not "what names are there",
 * so without a record of the names a key created by the CLI would be invisible to the server
 * that needs it. This file is that record.
 *
 * <p>It holds names only, never key material, so it needs no passphrase and no special
 * permissions. Losing it loses the ability to list, never the ability to sign: naming the alias
 * explicitly still works, which is why it is a convenience index rather than a source of truth.
 */
public final class AliasIndex {

  private final Path indexPath;

  /**
   * @param indexPath the file recording the aliases
   */
  public AliasIndex(@NonNull Path indexPath) {
    this.indexPath = indexPath;
  }

  /**
   * The recorded aliases.
   *
   * @return the aliases in the order they were added, empty when nothing was recorded
   */
  public List<String> read() {
    if (!Files.exists(indexPath)) {
      return List.of();
    }
    try {
      return Files.readAllLines(indexPath, StandardCharsets.UTF_8).stream()
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .distinct()
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read the alias index at " + indexPath, e);
    }
  }

  /**
   * Record an alias, ignoring one already present.
   *
   * @param alias the alias to remember
   */
  public void add(@NonNull String alias) {
    Set<String> aliases = new LinkedHashSet<>(read());
    if (aliases.add(alias)) {
      write(aliases);
    }
  }

  /**
   * Forget an alias.
   *
   * @param alias the alias to remove
   */
  public void remove(@NonNull String alias) {
    Set<String> aliases = new LinkedHashSet<>(read());
    if (aliases.remove(alias)) {
      write(aliases);
    }
  }

  private void write(Set<String> aliases) {
    try {
      Path parent = indexPath.toAbsolutePath().getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(indexPath, String.join("\n", aliases) + "\n", StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write the alias index at " + indexPath, e);
    }
  }
}
