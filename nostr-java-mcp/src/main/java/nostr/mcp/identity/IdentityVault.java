package nostr.mcp.identity;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.ISignable;
import nostr.base.PrivateKey;
import nostr.base.PublicKey;
import nostr.id.Identity;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the signing keys and does the signing, so nothing above it needs a key.
 *
 * <p>This is the module's central security boundary. Tools name an identity and receive a signed
 * event; they never receive an {@link Identity}, because an object that can sign is an object
 * that can be made to sign anything, and a tool layer that holds one is one bug away from
 * leaking it into a result.
 *
 * <p>Keys are wiped on shutdown. That is worth doing even though it cannot be complete: the SDK
 * derives its own copies internally, so wiping the vault's arrays shortens the window rather
 * than closing it. Claiming more than that would be dishonest, but a shorter window is still
 * worth having in a process an agent drives.
 */
@Slf4j
public final class IdentityVault implements AutoCloseable {

  private final Map<String, Identity> identitiesByAlias = new LinkedHashMap<>();
  private final Map<String, byte[]> keyMaterialByAlias = new LinkedHashMap<>();
  private final IdentityBinding binding;
  private volatile String defaultAlias;

  /**
   * Unlock every key the source holds, for a process that may sign as any of them.
   *
   * @param keySource where the keys come from
   * @param defaultAlias the identity used when a caller names none, or {@code null} for none
   */
  public IdentityVault(@NonNull KeySource keySource, String defaultAlias) {
    this(keySource, defaultAlias, IdentityBinding.unbound());
  }

  /**
   * Unlock the keys this process is permitted to hold.
   *
   * <p>A bound process refuses to start when its alias is missing. The alternative is a server
   * that runs, advertises signing tools, and fails at the moment an agent tries to use one; a
   * misconfigured binding is a startup problem and is reported where it can be fixed.
   *
   * @param keySource where the keys come from
   * @param defaultAlias the identity used when a caller names none, or {@code null} for none
   * @param binding which identities this process may operate
   * @throws IdentityUnknownException when a bound process's alias is not in the keystore
   */
  public IdentityVault(
      @NonNull KeySource keySource, String defaultAlias, @NonNull IdentityBinding binding) {
    this.binding = binding;
    warnIfUnprotected(keySource);
    keySource.loadKeys(binding).forEach(this::unlock);
    requireBoundIdentityWasFound(keySource);
    this.defaultAlias = resolveDefault(binding.alias().orElse(defaultAlias));
    announce(keySource);
  }

  /**
   * How this process is restricted, if at all.
   *
   * @return the binding this vault was built with
   */
  public IdentityBinding binding() {
    return binding;
  }

  /**
   * Fails a bound process whose identity does not exist, naming how to create one.
   *
   * <p>Only bound processes refuse. An unbound server with an empty keystore is how a person
   * creates their first key, so refusing there would make the module impossible to bootstrap.
   */
  private void requireBoundIdentityWasFound(KeySource keySource) {
    binding
        .alias()
        .filter(alias -> !identitiesByAlias.containsKey(alias))
        .ifPresent(
            alias -> {
              throw new KeystoreException(
                  "This server is bound to identity '"
                      + alias
                      + "', which the "
                      + keySource.type()
                      + " keystore does not hold. Create it with: java -jar nostr-java-mcp.jar"
                      + " keygen "
                      + alias);
            });
  }

  /**
   * The identities this vault can sign with, as an agent may see them.
   *
   * @return one summary per identity, carrying no key material
   */
  public List<IdentitySummary> list() {
    return identitiesByAlias.entrySet().stream()
        .map(entry -> IdentitySummary.of(entry.getKey(), entry.getValue().getPublicKey()))
        .toList();
  }

  /**
   * Look up one identity's public details.
   *
   * @param alias the identity to describe
   * @return its summary, or empty when the vault holds no such alias
   */
  public Optional<IdentitySummary> find(@NonNull String alias) {
    return Optional.ofNullable(identitiesByAlias.get(alias))
        .map(identity -> IdentitySummary.of(alias, identity.getPublicKey()));
  }

  /**
   * The alias used when a caller names none.
   *
   * @return the default alias, or empty when the vault holds none or several without a choice
   */
  public Optional<String> defaultAlias() {
    return Optional.ofNullable(defaultAlias);
  }

  /**
   * The public key of an identity, for addressing rather than signing.
   *
   * @param alias the identity to look up
   * @return its public key
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public PublicKey publicKeyOf(@NonNull String alias) {
    return require(alias).getPublicKey();
  }

  /**
   * Sign something as the named identity.
   *
   * <p>The only capability that crosses this boundary. A caller gets a signature, never the key
   * that produced it.
   *
   * @param alias the identity to sign as
   * @param signable what to sign
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public void signAs(@NonNull String alias, @NonNull ISignable signable) {
    require(alias).sign(signable);
  }

  /**
   * Take an identity into the vault, which becomes its owner.
   *
   * <p>The caller hands over the key material and must not keep it: the vault wipes what it
   * holds on shutdown, and a second copy elsewhere would outlive that.
   *
   * @param alias the name to hold it under
   * @param keyMaterial the private key bytes
   * @throws KeystoreException when the alias is already taken
   */
  public synchronized void add(@NonNull String alias, @NonNull byte[] keyMaterial) {
    if (identitiesByAlias.containsKey(alias)) {
      throw new KeystoreException("This server already holds an identity called '" + alias + "'");
    }
    unlock(alias, keyMaterial);
    if (defaultAlias == null && identitiesByAlias.size() == 1) {
      defaultAlias = alias;
    }
    log.info("Added identity '{}' ({})", alias, identitiesByAlias.get(alias).getPublicKey().toBech32String());
  }

  /**
   * Change what an identity is called, keeping the same key.
   *
   * @param currentAlias the existing name
   * @param newAlias the name to use instead
   * @throws IdentityUnknownException when the current alias is not held
   * @throws KeystoreException when the new alias is taken
   */
  public synchronized void rename(@NonNull String currentAlias, @NonNull String newAlias) {
    Identity identity = require(currentAlias);
    if (identitiesByAlias.containsKey(newAlias)) {
      throw new KeystoreException("This server already holds an identity called '" + newAlias + "'");
    }
    identitiesByAlias.remove(currentAlias);
    identitiesByAlias.put(newAlias, identity);
    keyMaterialByAlias.put(newAlias, keyMaterialByAlias.remove(currentAlias));
    if (currentAlias.equals(defaultAlias)) {
      defaultAlias = newAlias;
    }
    log.info("Renamed identity '{}' to '{}' ({})", currentAlias, newAlias, identity.getPublicKey().toBech32String());
  }

  /**
   * Forget an identity, wiping its key.
   *
   * <p>The public key is logged as it goes, so the audit trail outlives the key it describes:
   * afterwards there is nothing left to say which account was destroyed.
   *
   * @param alias the identity to remove
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public synchronized void remove(@NonNull String alias) {
    Identity identity = require(alias);
    log.info("Removing identity '{}' ({})", alias, identity.getPublicKey().toBech32String());
    byte[] keyMaterial = keyMaterialByAlias.remove(alias);
    if (keyMaterial != null) {
      Arrays.fill(keyMaterial, (byte) 0);
    }
    identitiesByAlias.remove(alias);
    if (alias.equals(defaultAlias)) {
      defaultAlias = identitiesByAlias.size() == 1 ? identitiesByAlias.keySet().iterator().next() : null;
    }
  }

  /**
   * Choose which identity signs when a caller names none.
   *
   * @param alias the identity to make default
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public synchronized void setDefault(@NonNull String alias) {
    require(alias);
    defaultAlias = alias;
    log.info("Default identity is now '{}'", alias);
  }

  /**
   * Build something that needs to sign, without handing over the key.
   *
   * <p>NIP-17 sealing and unwrapping cannot be expressed as "sign this": they derive shared
   * secrets, so the SDK's service takes an {@link Identity} rather than a signature. Rather than
   * release the key, the vault constructs the collaborator itself and returns only what that
   * collaborator exposes, which for {@code DirectMessageService} is composing and reading
   * messages and never the identity behind them.
   *
   * <p>The factory runs inside the vault and its result must not retain the identity beyond what
   * it needs, which is why this takes a factory rather than lending the identity out.
   *
   * @param alias the identity to build with
   * @param collaborator makes the object that needs to sign
   * @param <T> what is being built
   * @return the built object
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public synchronized <T> T using(
      @NonNull String alias, @NonNull java.util.function.Function<Identity, T> collaborator) {
    return collaborator.apply(require(alias));
  }

  /**
   * Hand back a copy of an identity's key material, for backing it up.
   *
   * <p>The one exception to keys never leaving the vault, and it is narrow on purpose: a backup
   * is the only thing that makes removal survivable, and it cannot be written without the key.
   * The caller gets a copy it must wipe, and the only caller is the backup writer, which puts
   * the bytes straight into an encrypted file and never into a tool result.
   *
   * @param alias the identity to export
   * @return a copy of the private key material
   * @throws IdentityUnknownException when the vault holds no such alias
   */
  public synchronized byte[] exportKeyMaterial(@NonNull String alias) {
    require(alias);
    byte[] keyMaterial = keyMaterialByAlias.get(alias);
    if (keyMaterial == null) {
      throw new KeystoreException("The key material for '" + alias + "' is no longer available");
    }
    return keyMaterial.clone();
  }

  /**
   * Whether this vault holds any identity at all.
   *
   * @return true when no key was loaded
   */
  public boolean isEmpty() {
    return identitiesByAlias.isEmpty();
  }

  private Identity require(String alias) {
    Identity identity = identitiesByAlias.get(alias);
    if (identity == null) {
      throw new IdentityUnknownException(alias, identitiesByAlias.keySet());
    }
    return identity;
  }

  private void unlock(String alias, byte[] keyMaterial) {
    identitiesByAlias.put(alias, Identity.create(new PrivateKey(keyMaterial)));
    keyMaterialByAlias.put(alias, keyMaterial);
  }

  /**
   * Choose the default, preferring an explicit one and falling back to a lone identity.
   *
   * <p>A single identity is unambiguous, so requiring a caller to name it would be pedantry.
   * Several identities with no explicit default stay ambiguous on purpose: guessing which
   * account to post from is a public, irreversible mistake, so signing fails instead.
   */
  private String resolveDefault(String requested) {
    if (requested != null && identitiesByAlias.containsKey(requested)) {
      return requested;
    }
    if (requested != null) {
      throw new IdentityUnknownException(requested, identitiesByAlias.keySet());
    }
    return identitiesByAlias.size() == 1 ? identitiesByAlias.keySet().iterator().next() : null;
  }

  private void warnIfUnprotected(KeySource keySource) {
    if (!keySource.protectsKeysAtRest()) {
      log.warn(
          "Keystore '{}' does not protect keys at rest and is unsuitable outside development",
          keySource.type());
    }
  }

  /** Announces what was unlocked, by public key only: a startup banner is still a log. */
  private void announce(KeySource keySource) {
    if (identitiesByAlias.isEmpty()) {
      log.info("Keystore '{}' holds no identities", keySource.type());
      return;
    }
    identitiesByAlias.forEach(
        (alias, identity) ->
            log.info("Unlocked identity '{}' ({})", alias, identity.getPublicKey().toBech32String()));
  }

  @Override
  public void close() {
    keyMaterialByAlias.values().forEach(key -> Arrays.fill(key, (byte) 0));
    keyMaterialByAlias.clear();
    identitiesByAlias.clear();
  }
}
