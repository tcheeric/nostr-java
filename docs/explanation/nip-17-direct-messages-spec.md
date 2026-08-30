# NIP-17 Private Direct Messages: Implementation Specification (Draft)

This document specifies how to add [NIP-17](https://github.com/nostr-protocol/nips/blob/master/17.md)
private direct messages, and the [NIP-59](https://github.com/nostr-protocol/nips/blob/master/59.md)
gift-wrap machinery it stands on, to nostr-java. It is a design explanation: it states what
exists today, what must be built, where each piece belongs, and the constraints the current
codebase imposes. It is a prerequisite for the direct-message tools in the
[nostr-java-mcp spec](nostr-java-mcp-spec.md), but it stands on its own: gift wrapping is
useful to every consumer of the SDK.

## 1. Motivation

The SDK's only direct-message support is NIP-04 (`EncryptedDirectMessage`, kind 4). NIP-04
is deprecated in practice and leaks metadata badly: the sender pubkey, the recipient `p`
tag, the exact timestamp, and the message count are all public on every relay that carries
the event. An observer learns who talks to whom and when, and only the message text is
hidden.

NIP-17 fixes this by triple-layering. An **unsigned rumor** carries the content, a
**kind-13 seal** signed by the real sender hides it from everyone but the recipient, and a
**kind-1059 gift wrap** signed by a fresh throwaway key hides the sender. With randomised
timestamps on the outer two layers, a relay sees only "some random key sent something to
this pubkey at roughly this time".

Building it means the SDK can offer private messaging that is actually private, and gains a
reusable gift-wrap primitive that NIP-59 explicitly intends other protocols to build on.

## 2. What already exists

| Piece | Status | Location |
| --- | --- | --- |
| NIP-44 v2 encrypt/decrypt | Present | `nostr.crypto.nip44.EncryptedPayloads` (core) |
| NIP-44 conversation key (ECDH + HKDF) | Present | `EncryptedPayloads.getConversationKey` |
| NIP-44 cipher facade | Present | `nostr.encryption.MessageCipher44` (identity) |
| Schnorr signing, key generation | Present | `Schnorr`, `Identity`, `PrivateKey.generateRandomPrivKey` |
| Event id computation over an unsigned event | Present | `EventSerializer.computeEventId` |
| Event JSON encode/decode | Present | `BaseEventEncoder`, `EventJsonMapper` |
| Kind constants | Partial | `Kinds` has `ENCRYPTED_DIRECT_MESSAGE = 4`; 13/14/15/1059/10050 missing |
| Rumor (unsigned event) as a first-class concept | **Missing** | — |
| Seal, gift wrap, unwrapping | **Missing** | — |
| DM relay list (kind 10050) | **Missing** | — |

The cryptography is done. What is missing is the event-composition layer above it.

## 3. Constraints in the current codebase

Three facts about the existing code shape the design, and each needs an explicit decision.

### 3.1 `GenericEvent.update()` forces `created_at` to now

```java
public void update() {
  this.createdAt = Instant.now().getEpochSecond();
  ...
}
```

NIP-59 requires the seal and gift wrap to carry a **randomised** `created_at`, up to two
days in the past, precisely to defeat timing correlation. The current `update()` makes that
impossible: any caller that computes an id also overwrites the timestamp.

**Resolution.** Split the two responsibilities. `update()` keeps its current behaviour for
backwards compatibility but delegates to a new `update(long createdAt)` that does not touch
the clock. This is additive, changes no existing call site's behaviour, and gives the
gift-wrap code the seam it needs. The alternative, a `Clock`/`Supplier<Long>` injected into
`GenericEvent`, is a larger change to a widely-used class for no extra benefit here.

### 3.2 `MessageCipher44` takes raw key bytes and prefixes `02`

```java
EncryptedPayloads.getConversationKey(
    NostrUtil.bytesToHex(senderPrivateKey), "02" + NostrUtil.bytesToHex(recipientPublicKey));
```

Usable as-is. NIP-17 needs the same conversation-key derivation with two different key
pairs per message (sender↔recipient for the seal, ephemeral↔recipient for the wrap), which
is just two `MessageCipher44` instances. No change required, but the gift-wrap code must
construct ciphers rather than assume one conversation.

### 3.3 A rumor is an event that must never be signed

`GenericEvent` implements `ISignable` and `validate()` requires a signature. A rumor has an
`id` and no `sig`, and encoding it must emit `"sig"` absent (or empty) without the encoder
rejecting it.

**Resolution.** Model the rumor as its own type rather than as a `GenericEvent` in a
half-built state (see §4.1). Making illegal states unrepresentable is worth more here than
reusing the class, because the whole security property of NIP-59 rests on a rumor never
acquiring a signature.

## 4. Design

All new code lands in `nostr-java-event` and `nostr-java-identity`. Nothing in `core`
changes; nothing in `client` changes.

### 4.1 New types in `nostr-java-event`

**`Rumor`** — an unsigned event. Holds `pubkey`, `createdAt`, `kind`, `tags`, `content`, and
a derived `id`. It is deliberately **not** an `ISignable` and exposes no signature field, so
a rumor cannot be signed by accident. It serialises to the same JSON shape as an event with
`"sig": ""`.

**`Kinds` additions**:

```java
public static final int SEAL = 13;
public static final int CHAT_MESSAGE = 14;
public static final int FILE_MESSAGE = 15;
public static final int GIFT_WRAP = 1059;
public static final int EPHEMERAL_GIFT_WRAP = 21_059;
public static final int DM_RELAY_LIST = 10_050;
```

**`ChatMessage`** — the kind-14 rumor: content, recipients, optional `subject`, optional `e`
tag for a reply parent. Built through a builder; it is the only type an application author
should need to touch for the common case.

**`DirectMessageRelayList`** — the kind-10050 event, a list of `relay` tags. Needed because
NIP-17 states clients MUST only publish DMs to relays in the recipient's kind-10050 list,
and MUST NOT send at all when no list is found. Publishing a DM to an arbitrary relay is a
protocol violation, so this is not optional.

### 4.2 New capability in `nostr-java-identity`: `GiftWrapper`

Signing lives in `identity`, and gift wrapping is fundamentally a signing operation with
two keys, so the wrap/unwrap logic belongs there alongside `MessageCipher`.

```java
public interface GiftWrapper {
  GenericEvent wrap(Rumor rumor, PublicKey recipient);
  Rumor unwrap(GenericEvent giftWrap);
}
```

Two methods, one responsibility: hide a rumor, and reveal it. Everything in §5 is an
implementation detail behind this interface, which is what makes it a deep module — a large
amount of protocol behaviour behind a surface an application author can hold in their head.

`Nip59GiftWrapper` is the implementation, constructed with the sender's `Identity`. A second
implementation for `kind:21059` ephemeral wraps differs only in the outer kind, so the wrap
kind is a constructor parameter rather than a flag argument on the method.

### 4.3 The DM service

`DirectMessageService`, also in `identity`, is the NIP-17-specific layer above the
NIP-59-generic `GiftWrapper`:

```java
List<GenericEvent> compose(ChatMessage message);   // one gift wrap per recipient, plus self
ChatMessage read(GenericEvent giftWrap);           // unwrap, verify, decode
```

`compose` returning a **list** is not incidental: NIP-17 requires a separate gift wrap per
recipient *and* one addressed to the sender, since the sender cannot otherwise read their
own history. Making that plurality visible in the signature stops callers from publishing
one event and silently losing their outbox.

## 5. The algorithm

### Sending

1. Build the kind-14 rumor: real sender pubkey, real `created_at`, `p` tag per recipient,
   optional `subject` and `e` tags. Compute its id. **Do not sign it.**
2. For each recipient, and once more for the sender's own pubkey:
   1. Encrypt the JSON rumor with NIP-44 under `conversationKey(senderPrivkey, recipientPubkey)`.
   2. Put the ciphertext in a kind-13 seal with **empty tags**, `created_at` randomised up
      to two days in the past, and sign it with the sender's key.
   3. Generate a **fresh** random keypair, used for exactly one wrap and then discarded.
   4. Encrypt the JSON seal under `conversationKey(ephemeralPrivkey, recipientPubkey)`.
   5. Put that in a kind-1059 gift wrap with a single `p` tag for the recipient, an
      **independently** randomised past `created_at`, and sign it with the ephemeral key.
3. Fetch each recipient's kind-10050 list and publish their wrap only to those relays. If a
   recipient has no list, do not send; report it.

### Receiving

1. Subscribe to kind 1059 with `#p` = your pubkey.
2. NIP-44 decrypt `content` with `conversationKey(yourPrivkey, giftWrap.pubkey)`.
3. Parse the seal. **Verify its signature.**
4. NIP-44 decrypt the seal's content with `conversationKey(yourPrivkey, seal.pubkey)`.
5. Parse the rumor and **verify `rumor.pubkey == seal.pubkey`.**
6. Recompute the rumor's id and check it matches.

### Security rules, stated as invariants

These are the parts a naive implementation gets wrong, so each becomes a named check with
its own test:

- **The impersonation check.** A rumor whose `pubkey` differs from the sealing key's is
  rejected. NIP-17 calls this out explicitly: without it, anyone can forge a message from
  anyone by changing one field. The rumor is unsigned, so the seal's signature is the *only*
  thing binding content to author.
- **The seal signature is verified**, not assumed. An unverified seal is an unauthenticated
  message.
- **Ephemeral keys are single-use** and never persisted, logged, or reused across
  recipients. Reuse links wraps together and undoes the whole scheme.
- **Seal tags are always empty.** Any tag on a kind-13 leaks to an observer who can see the
  seal.
- **Timestamps are independently randomised** per layer, and never in the future, since many
  relays drop future-dated events.
- **A rumor is never signed.** Enforced by the type system (§4.1), not by discipline.
- **Failure to decrypt is not an error condition.** A subscription to kind 1059 receives
  wraps addressed to you but also, potentially, garbage. Undecryptable wraps are skipped,
  not thrown, or one malformed event stops a whole inbox from loading.

## 6. Public API sketch

```java
Identity alice = Identity.create(alicePrivateKey);
DirectMessageService messages = new Nip17DirectMessageService(alice);

ChatMessage message = ChatMessage.builder()
    .to(bobPublicKey)
    .subject("Dinner")
    .content("Are you going to the party tonight?")
    .build();

// One wrap for Bob, one for Alice's own copy.
List<GenericEvent> wraps = messages.compose(message);

// Read side.
ChatMessage received = messages.read(incomingGiftWrap);
```

The application author never sees a seal, an ephemeral key, or a conversation key. That is
the point.

## 7. Scope

**In scope for this work:** kind-14 chat messages, NIP-59 seal and gift wrap (kinds 13,
1059, 21059), unwrapping with full verification, kind-10050 DM relay lists, and the
`Rumor` type.

**Out of scope, deliberately:** kind-15 file messages (needs AES-GCM file encryption and an
upload story, and is a separate NIP-96 shaped problem); disappearing messages via
`expiration` tags; NIP-42 AUTH, which relays require to serve gift wraps and which the
client module must add separately; group chats beyond a handful of recipients, which NIP-17
itself says to avoid. Kind-15 and expiration are noted here because the `Rumor` and
`GiftWrapper` types must not preclude them: any rumor kind is wrappable, which they already
allow.

NIP-04 is left in place, deprecated in Javadoc, and not removed. Removing it is a breaking
change and a separate decision.

## 8. Testing strategy

- **Vectors from the NIPs.** NIP-59 §"An Example" gives an author key, recipient key,
  ephemeral key, and the exact resulting seal and gift wrap. With the ephemeral key and both
  timestamps injected, our output must match byte for byte. NIP-17 supplies a second pair of
  wraps. These are the highest-value tests available and they pin the implementation to the
  spec rather than to our reading of it.
- **Round trip**: `unwrap(wrap(rumor)) == rumor`, over generated content including Unicode,
  empty strings, and the NIP-44 maximum plaintext size.
- **The impersonation attack**: hand-build a wrap whose rumor pubkey differs from the seal
  pubkey and assert it is rejected. This test is the reason the check exists, so it must
  fail loudly if the check is ever removed.
- **Adversarial inputs**: a wrap encrypted to someone else, a corrupted ciphertext, a
  kind-13 with a bad signature, a kind-13 carrying tags, a rumor with a mismatched id, and a
  future-dated wrap. Each has a defined outcome.
- **Non-determinism**: two wraps of the same rumor produce different ephemeral pubkeys,
  different ciphertexts, and different `created_at` values, all within the two-day window
  and none in the future. Randomness is injected so the assertions are deterministic.
- **Property test**: over many random keypairs and messages, the round trip holds and no
  ephemeral key ever repeats.
- **Integration**: publish a gift wrap to the Docker test relay, subscribe as the recipient,
  and read the message back.
- Run with `mvn -q verify` from the repository root.

## 9. Delivery plan

1. `Kinds` constants, `Rumor` type and its JSON codec, `GenericEvent.update(long)`.
2. `Nip59GiftWrapper`: wrap and unwrap with all §5 invariants, validated against the NIP-59
   vectors.
3. `ChatMessage` (kind 14) and `Nip17DirectMessageService`, validated against the NIP-17
   vectors.
4. `DirectMessageRelayList` (kind 10050) and relay-selection on publish.
5. Documentation: a how-to for sending and reading private messages, and a `CHANGELOG.md`
   entry under `Added`.

Phases 1–3 are what the MCP module's DM tools need; phase 4 is required before any real
network use, since publishing without a recipient's relay list violates the NIP.

## 10. Open questions

- Should `GiftWrapper` live in `nostr-java-identity` (with signing, as proposed) or in
  `nostr-java-event` (with event construction)? It genuinely needs both, and the deciding
  factor is that it must hold a private key.
- Randomness injection: constructor-supplied `SecureRandom`, or a package-private seam used
  only by tests? The vector tests need determinism without widening the public API.
- Does `Rumor` warrant its own type, or is a `GenericEvent` with a null signature and a
  documented convention enough? This spec argues for the type; it costs a class and a codec.
- Should NIP-04 be deprecated with `@Deprecated` in this change, signalling a removal in the
  next major version?
- NIP-42 AUTH in `nostr-java-client` is a hard dependency for real-world DM delivery, since
  relays are told to gate kind-1059 behind it. Should it be pulled into this work or tracked
  as its own?

## Related documents

- [nostr-java-mcp-spec.md](nostr-java-mcp-spec.md) — the MCP module that consumes this work
- [extending-events.md](extending-events.md) — how custom events and tags are modelled
- [architecture.md](architecture.md) — module boundaries this design respects
- [../howto/custom-events.md](../howto/custom-events.md) — working with custom event kinds
