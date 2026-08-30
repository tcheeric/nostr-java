# 09: Threads, contacts and direct messages

**What to build:** An agent can follow a conversation, read who someone follows, and send a
private message that only its recipient can read.

Direct messages need no SDK work: NIP-17 gift wrapping shipped in 2.1.0 and delivery to a
recipient's own relays in 2.2.0, so `nostr_send_direct_message` is a thin adapter over
`NostrClient.sendDirectMessage`. Two behaviours must be surfaced rather than hidden, both
verified against a live relay:

- A recipient who published no kind-10050 relay list is `UNREACHABLE`, because NIP-17 forbids
  sending to them. The message genuinely did not go, and the user must be told who missed it.
- Every conversation includes the sender, so a one-recipient send reports **two** outcomes. A
  sender without their own relay list sees their archival copy come back `UNREACHABLE` while
  the recipient is `DELIVERED`. Reported as "1 of 2 delivered", that would tell a user their
  message failed when it arrived perfectly well.

**Blocked by:** 01 (`ContactList` type over kind-3), 06 (`WriteGuard` and the publish tools),
08 (Long-lived subscriptions).

**Status:** ready-for-agent

- [x] `nostr_fetch_thread` resolves a note and its replies per NIP-10
- [x] `nostr_get_contacts` reads a kind-3 list through the SDK's `ContactList` type
- [x] `nostr_send_direct_message` delivers to each recipient's own relays and reports per
      recipient
- [x] A recipient without a relay list is reported `UNREACHABLE`, not silently skipped
- [x] The sender's own copy is reported separately from the recipients, so it is never counted
      as a failed delivery
- [x] `nostr_read_direct_messages` unwraps gift wraps addressed to an identity
- [x] DM decryption is opt-in per identity, since it exposes private correspondence to the model
- [x] NIP-04 is not exposed at all
- [x] `mvn -q verify` passes
